package com.hellcat.movie_app.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellcat.movie_app.config.MovieWebSocketHandler;
import com.hellcat.movie_app.dto.MovieDto;
import com.hellcat.movie_app.entity.ImportHistory;
import com.hellcat.movie_app.repository.ImportHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final MovieService movieService;
    private final ImportHistoryRepository historyRepository;
    private final MovieWebSocketHandler webSocketHandler;

    @Lazy
    @Autowired
    private ImportService self;

    // Фабрики для создания парсеров
    private final JsonFactory jsonFactory = new JsonFactory();
    private YAMLFactory yamlFactory;
    // Инициализируем YAMLFactory в конструкторе или блоке инициализации
    {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setCodePointLimit(500 * 1024 * 1024); // Ставим лимит 500 МБ (хватит для вашего файла)
        yamlFactory = YAMLFactory.builder()
                .loaderOptions(loaderOptions)
                .build();
    }
    private final ObjectMapper mapper = new ObjectMapper(); // Один маппер для десериализации узлов

    public void handleImport(MultipartFile[] files) {
        for (MultipartFile file : files) {
            try {
                Path tempPath = Files.createTempFile("import_job_", "_" + file.getOriginalFilename());
                File tempFile = tempPath.toFile();
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
                }
                self.processFileAsync(tempFile, file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Failed to prepare file", e);
            }
        }
    }

    @Async("taskExecutor")
    public void processFileAsync(File fileOnDisk, String originalFileName) {
        log.info("Started processing file: {}", originalFileName); // <-- ЛОГ В КОНСОЛЬ
        ImportHistory history = new ImportHistory();
        history.setFileName(originalFileName);
        history.setStatus("PROCESSING");
        history.setLogInfo("Start processing...\n");
        history = historyRepository.save(history);

        // Пул потоков для ЭТОГО файла
        int maxWorkers = 10;
        ThreadPoolExecutor fileExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        // Важно: LinkedBlockingQueue без ограничений не даст создавать новые потоки,
        // поэтому используем SynchronousQueue или управляем setCorePoolSize вручную (как ниже)

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try (InputStream inputStream = new FileInputStream(fileOnDisk)) {

            // Выбираем фабрику в зависимости от расширения
            JsonFactory factory = (originalFileName.endsWith(".yaml") || originalFileName.endsWith(".yml"))
                    ? yamlFactory : jsonFactory;

            // --- STREAMING PARSING ---
            try (JsonParser parser = factory.createParser(inputStream)) {

                // Пропускаем начало массива [
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new IllegalStateException("Expected content to be an array");
                }

                int batchSize = 100;
                List<MovieDto> batch = new ArrayList<>();
                List<Future<?>> futures = new ArrayList<>();

                // Читаем, пока не встретим конец массива ]
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    // Читаем один объект MovieDto
                    MovieDto dto = mapper.readValue(parser, MovieDto.class);
                    batch.add(dto);

                    if (batch.size() >= batchSize) {
                        final List<MovieDto> taskBatch = new ArrayList<>(batch);
                        futures.add(fileExecutor.submit(() -> processBatch(taskBatch, successCount, failCount)));
                        batch.clear();

                        // --- ЛОГИКА ВОРКЕРОВ ---
                        long elapsedTime = System.currentTimeMillis() - startTime;

                        // Если прошло больше 3 секунд от начала
                        if (elapsedTime > 300) {
                            int currentCore = fileExecutor.getCorePoolSize();
                            if (currentCore < maxWorkers) {
                                // Увеличиваем пул
                                int newSize = currentCore + 1;
                                fileExecutor.setMaximumPoolSize(newSize);
                                fileExecutor.setCorePoolSize(newSize);

                                String logMsg = String.format("[Time: %d ms] Too slow! Adding worker. Total workers: %d\n", elapsedTime, newSize);
                                log.info(logMsg); // <-- ЛОГ В КОНСОЛЬ (его будет видно в docker logs)
                                self.updateLogSafe(history.getId(), logMsg);
                                log.info(logMsg);
                                startTime = System.currentTimeMillis();
                            }
                        }
                    }
                }

                // Обработка остатка
                if (!batch.isEmpty()) {
                    final List<MovieDto> taskBatch = new ArrayList<>(batch);
                    futures.add(fileExecutor.submit(() -> processBatch(taskBatch, successCount, failCount)));
                }

                // Ждем всех
                for (Future<?> future : futures) {
                    try { future.get(); } catch (Exception e) { log.error("Batch error", e); }
                }
            }
            // -------------------------

            log.info("File processed successfully: {}", originalFileName);
            history.setStatus("SUCCESS");
            self.updateLogSafe(history.getId(), "Done! Total success: " + successCount.get() + "\n");

        } catch (Exception e) {
            log.error("Error processing file: " + originalFileName, e);
            history.setStatus("FAILED");
            self.updateLogSafe(history.getId(), "Error: " + e.getMessage());
        } finally {
            fileExecutor.shutdown();
            history.setEndTime(LocalDateTime.now());
            history.setAddedCount(successCount.get());
            history.setFailedCount(failCount.get());
            historyRepository.save(history);

            try {
                // Уведомление на фронт
                webSocketHandler.broadcast(new WebSocketMessage("BULK_UPDATE", "Import finished"));
                // Можно добавить специальный тип для обновления только таблицы истории
                // webSocketHandler.broadcast(new WebSocketMessage("HISTORY_UPDATE", null));
            } catch (Exception e) {
                log.error("Socket error", e);
            }

            if (fileOnDisk.exists()) {
                fileOnDisk.delete();
            }
        }
    }

    private void processBatch(List<MovieDto> batch, AtomicInteger success, AtomicInteger fail) {
        // Искусственная задержка, чтобы на больших файлах было видно добавление воркеров
        // На реальном проде уберите, если база справляется быстро
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (MovieDto dto : batch) {
            try {
                movieService.create(dto);
                success.incrementAndGet();
            } catch (Exception e) {
                fail.incrementAndGet();
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLogSafe(Long historyId, String message) {
        try {
            ImportHistory h = historyRepository.findById(historyId).orElse(null);
            if (h != null) {
                String current = h.getLogInfo() == null ? "" : h.getLogInfo();
                // Обрезаем лог, чтобы не переполнять базу и сеть
                if (current.length() > 20000) {
                    current = current.substring(current.length() - 19000); // Оставляем только конец
                }
                h.setLogInfo(current + message);
                historyRepository.saveAndFlush(h);

                // ВАЖНО: Можно не слать сокет здесь, так как ImportPage.jsx
                // сам опрашивает сервер (polling) каждые 2 секунды.
                // Главное - чтобы запись попала в базу (saveAndFlush это делает).
            }
        } catch (Exception e) {
            // Пишем в консоль, чтобы видеть проблему
            log.error("Failed to write import log to DB", e);
        }
    }

    @Data
    @AllArgsConstructor
    private static class WebSocketMessage {
        private String type; // Поле 'type' вместо 'action'
        private Object payload;
    }
}