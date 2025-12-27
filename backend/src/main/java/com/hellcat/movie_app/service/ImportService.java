package com.hellcat.movie_app.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellcat.movie_app.config.MovieWebSocketHandler;
import com.hellcat.movie_app.config.RabbitConfig;
import com.hellcat.movie_app.config.WebSocketMessage;
import com.hellcat.movie_app.dto.ImportTaskDto;
import com.hellcat.movie_app.dto.MovieDto;
import com.hellcat.movie_app.entity.ImportHistory;
import com.hellcat.movie_app.exception.BadFileException;
import com.hellcat.movie_app.repository.ImportHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportHistoryRepository historyRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MovieWebSocketHandler webSocketHandler;
    private final StorageService storageService; // Сервис MinIO

    @Lazy
    @Autowired
    private ImportService self;

    private final JsonFactory jsonFactory = new JsonFactory();
    private final YAMLFactory yamlFactory = YAMLFactory.builder()
            .loaderOptions(new LoaderOptions() {{
                setCodePointLimit(500 * 1024 * 1024); // Настройка лимита
                }})
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Точка входа. Реализует распределенную транзакцию (паттерн TCC/Saga simplified).
     */
    public void handleImport(MultipartFile[] files) {
        for (MultipartFile file : files) {
            try {
                Path tempPath = Files.createTempFile("import_upload_", "_" + file.getOriginalFilename());
                File tempFile = tempPath.toFile();
                file.transferTo(tempFile);
                self.prepareAndProcessAsync(tempFile, file.getOriginalFilename(), file.getContentType());
            } catch (Exception e) {
                log.error("Failed to locally prepare file", e);
            }
        }
    }

    @Async("taskExecutor")
    public void prepareAndProcessAsync(File tempFile, String originalFilename, String contentType) {
        String objectName = UUID.randomUUID() + "_" + originalFilename;
        ImportHistory history = null;

        try {
            // ШАГ 1: Фиксируем намерение (State: UPLOADING)
            // Создаем запись в БД СРАЗУ. Теперь у нас есть ID транзакции.
            history = self.createInitialHistoryRecord(originalFilename, objectName);

            log.info("Transaction started. ID: {}, MinIO Name: {}", history.getId(), objectName);

            // ШАГ 2: Загрузка в MinIO
            // Это самая опасная операция, но мы уже "в домике" (запись в БД есть)
            try (InputStream in = new FileInputStream(tempFile)) {
                storageService.uploadFile(objectName, in, contentType);
            }

            log.info("Uploaded to MinIO. Updating status...");

            // Имитация сбоя (раскомментировать для защиты)
            // if (originalFilename.contains("fail")) throw new RuntimeException("MinIO is unreachable (Simulated)");

            // ШАГ 3: Обновляем статус на QUEUED (готов к обработке)
            self.updateHistoryStatus(history.getId(), "QUEUED", "Upload successful. Starting parsing...\n");

            // ШАГ 4: Запускаем парсинг
            // Здесь мы не передаем файл, мы скачиваем его обратно из MinIO для проверки целостности
            // (или используем локальный tempFile, но лучше скачать, чтобы проверить, что MinIO работает)
            self.processFileAsync(tempFile, history.getId(), originalFilename);

        } catch (Exception e) {
            log.error("Import failed at upload stage", e);

            // КОМПЕНСАЦИЯ:
            // Мы не удаляем файл (вдруг MinIO лежит), мы помечаем запись как FAILED.
            // Теперь у админа есть запись "Ошибка загрузки".
            if (history != null) {
                self.updateHistoryStatus(history.getId(), "FAILED", "Upload failed: " + e.getMessage());
            }
        } finally {
            // Локальный временный файл больше не нужен
            // (В processFileAsync мы его используем, поэтому удаляем там, либо передаем копию)
            // В данной реализации я передаю tempFile дальше, поэтому здесь не удаляю.
            // Удаление будет в processFileAsync.
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ТРАНЗАКЦИОННЫЕ МЕТОДЫ ---

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportHistory createInitialHistoryRecord(String filename, String objectName) {
        ImportHistory h = new ImportHistory();
        h.setFileName(filename);
        h.setMinioObjectName(objectName);
        h.setStatus("UPLOADING"); // <-- Промежуточный статус
        h.setLogInfo("Initiating upload to storage...\n");
        return historyRepository.saveAndFlush(h);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateHistoryStatus(Long id, String status, String logAppend) {
        ImportHistory h = historyRepository.findById(id).orElse(null);
        if (h != null) {
            h.setStatus(status);
            String currentLog = h.getLogInfo() == null ? "" : h.getLogInfo();
            h.setLogInfo(currentLog + logAppend);
            historyRepository.saveAndFlush(h);
        }
    }


    /**
     * Асинхронный метод парсинга и отправки в очередь.
     */
    @Async("taskExecutor")
    public void processFileAsync(File fileOnDisk, Long historyId, String originalFileName) {
        log.info("Async processing started for historyId: {}", historyId);
        self.updateLogSafe(historyId, "Reading file and preparing batches...\n");

        int batchSize = 200;
        int totalSent = 0;

        try (InputStream inputStream = new FileInputStream(fileOnDisk)) {
            JsonFactory factory = (originalFileName.endsWith(".yaml") || originalFileName.endsWith(".yml"))
                    ? yamlFactory : jsonFactory;

            try (JsonParser parser = factory.createParser(inputStream)) {
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new IllegalStateException("Expected JSON/YAML array");
                }

                List<MovieDto> batch = new ArrayList<>();

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    MovieDto dto = mapper.readValue(parser, MovieDto.class);
                    batch.add(dto);

                    if (batch.size() >= batchSize) {
                        sendBatchToRabbit(historyId, originalFileName, batch);
                        totalSent += batch.size();
                        batch.clear();
                    }
                }

                if (!batch.isEmpty()) {
                    sendBatchToRabbit(historyId, originalFileName, batch);
                    totalSent += batch.size();
                }

                historyRepository.setExpectedCount(historyId, totalSent);

                String msg = String.format("File parsed. Sent %d records to Queue. Workers are processing...\n", totalSent);
                self.updateLogSafe(historyId, msg);
            }
        } catch (Exception e) {
            log.error("Error processing file {}", originalFileName, e);
            ImportHistory h = historyRepository.findById(historyId).orElse(null);
            if (h != null) {
                h.setStatus("FAILED");
                historyRepository.save(h);
                self.updateLogSafe(historyId, "Error: " + e.getMessage());
            }
        } finally {
            if (fileOnDisk.exists()) {
                fileOnDisk.delete();
            }
        }
    }

    private void sendBatchToRabbit(Long historyId, String fileName, List<MovieDto> batch) {
        ImportTaskDto task = new ImportTaskDto(historyId, fileName, new ArrayList<>(batch));
        rabbitTemplate.convertAndSend(RabbitConfig.IMPORT_QUEUE, task);
    }

    /**
     * Безопасное обновление лога в отдельной транзакции.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLogSafe(Long historyId, String message) {
        try {
            ImportHistory h = historyRepository.findById(historyId).orElse(null);
            if (h != null) {
                String current = h.getLogInfo() == null ? "" : h.getLogInfo();
                if (current.length() > 50000) current = current.substring(0, 49000) + "\n...truncated...";
                h.setLogInfo(current + message);
                historyRepository.saveAndFlush(h);
            }
        } catch (Exception e) {
            log.error("Failed to update log", e);
        }
    }
}