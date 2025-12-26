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

    // --- Настройка парсеров ---
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
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            log.info("Starting distributed transaction for file: {}", file.getOriginalFilename());

            try {
                // ШАГ 1: Загрузка в MinIO (Внешний ресурс 1)
                // Если здесь упадет, ничего страшного, в БД ничего нет.
                try (InputStream in = file.getInputStream()) {
                    storageService.uploadFile(objectName, in, file.getContentType());
                }
                log.info("Step 1: Uploaded to MinIO as {}", objectName);

                // Имитация сбоя для проверки (раскомментировать для защиты)
                // if (file.getOriginalFilename().contains("fail")) throw new RuntimeException("Simulated Logic Error");

                // ШАГ 2: Сохранение метаданных в БД и запуск обработки (Внешний ресурс 2)
                // Если упадет здесь, сработает catch, и мы удалим файл из MinIO.
                self.createImportRecordAndStartProcessing(file.getOriginalFilename(), objectName);

                log.info("Step 2: Database record created. Transaction complete.");

            } catch (Exception e) {
                log.error("Distributed transaction FAILED. Initiating ROLLBACK for {}", objectName, e);

                // --- КОМПЕНСАЦИЯ (ROLLBACK) ---
                try {
                    storageService.deleteFile(objectName);
                    log.info("Rollback SUCCESS: File deleted from MinIO");
                } catch (Exception ex) {
                    // Это критическая ситуация (нужен ручной разбор или ретрайер)
                    log.error("Rollback FAILED: Could not delete file from MinIO. Orphan file: {}", objectName, ex);
                }

                // Важно: не проглатываем исключение, чтобы клиент получил 500
                throw new RuntimeException("Import failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Создает запись в БД и инициирует асинхронный процесс.
     * Выделено в отдельный метод для управления транзакцией БД.
     */
    @Transactional
    public void createImportRecordAndStartProcessing(String originalFileName, String objectName) throws Exception {
        // 1. Создаем запись в истории
        ImportHistory history = new ImportHistory();
        history.setFileName(originalFileName);
        history.setMinioObjectName(objectName); // Ссылка на файл в MinIO
        history.setStatus("QUEUED");
        history.setLogInfo("Uploaded to storage. Downloading for processing...\n");

        ImportHistory savedHistory = historyRepository.saveAndFlush(history); // Коммит в БД будет после выхода из метода

        // 2. Скачиваем файл из MinIO во временный локальный файл для парсинга
        // (Парсить поток из сети напрямую опасно из-за таймаутов, надежнее скачать локально)
        Path tempPath = Files.createTempFile("import_proc_", "_" + originalFileName);
        File tempFile = tempPath.toFile();

        try (InputStream in = storageService.getFile(objectName)) {
            Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 3. Запускаем асинхронный парсинг
        // Передаем ID истории, а не объект, чтобы избежать Detached Entity проблем
        self.processFileAsync(tempFile, savedHistory.getId(), originalFileName);
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
                // Проверка на массив
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

                // Фиксируем общее количество для отслеживания прогресса
                historyRepository.setExpectedCount(historyId, totalSent);

                String msg = String.format("File parsed. Sent %d records to Queue. Workers are processing...\n", totalSent);
                self.updateLogSafe(historyId, msg);
            }
        } catch (Exception e) {
            log.error("Error processing file {}", originalFileName, e);
            // Обновляем статус на FAILED
            ImportHistory h = historyRepository.findById(historyId).orElse(null);
            if (h != null) {
                h.setStatus("FAILED");
                historyRepository.save(h);
                self.updateLogSafe(historyId, "Error: " + e.getMessage());
            }
        } finally {
            // Удаляем временный локальный файл (в MinIO он остается)
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
            // Используем нативный SQL update для лога тоже, чтобы избежать блокировок
            // historyRepository.appendLog(historyId, message);
            // Но для простоты оставим JPA saveAndFlush, т.к. этот метод вызывает только ОДИН поток (парсера)

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