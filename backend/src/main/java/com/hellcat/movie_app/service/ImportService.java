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
                // ШАГ 1 (СИНХРОННО): Сохраняем файл на диск, пока HTTP запрос жив
                Path tempPath = Files.createTempFile("import_upload_", "_" + file.getOriginalFilename());
                File tempFile = tempPath.toFile();
                file.transferTo(tempFile); // Или FileCopyUtils.copy

                // ШАГ 2: Запускаем асинхронную обработку, передавая File, а не MultipartFile
                self.prepareAndProcessAsync(tempFile, file.getOriginalFilename(), file.getContentType());

            } catch (Exception e) {
                log.error("Failed to prepare file for import", e);
                // Тут можно кинуть исключение, чтобы пользователь узнал сразу
            }
        }
    }


    /**
    * --- АСИНХРОННАЯ ОБЕРТКА ---
    * Принимает File, имя и тип контента
     * */
    @Async("taskExecutor")
    public void prepareAndProcessAsync(File tempFile, String originalFilename, String contentType) {
        String objectName = UUID.randomUUID() + "_" + originalFilename;
        log.info("Starting distributed transaction for file: {}", originalFilename);

        try {
            try (InputStream in = new FileInputStream(tempFile)) {
                storageService.uploadFile(objectName, in, contentType);
            }
            log.info("Step 1: Uploaded to MinIO as {}", objectName);

            // DEMO
            if (originalFilename.contains("fail")) {
                log.error("SIMULATING SERVER CRASH / LOGIC ERROR...");
                throw new RuntimeException("Simulated Logic Error between MinIO and DB");
            }

            self.createImportRecordAndStartProcessing(originalFilename, objectName);

            log.info("Step 2: Database record created. Transaction complete.");

        } catch (Exception e) {
            log.error("Distributed transaction FAILED. Initiating ROLLBACK for {}", objectName, e);
            try {
                storageService.deleteFile(objectName); // Компенсация
                log.info("Rollback SUCCESS");
            } catch (Exception ex) {
                log.error("Rollback FAILED", ex);
            }
        } finally {
            // ВАЖНО: Удаляем временный файл, который мы создали в handleImport
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Transactional
    public void createImportRecordAndStartProcessing(String originalFileName, String objectName) throws Exception {
        ImportHistory history = new ImportHistory();
        history.setFileName(originalFileName);
        history.setMinioObjectName(objectName);
        history.setStatus("QUEUED");
        history.setLogInfo("Uploaded to storage. Downloading for processing...\n");

        ImportHistory savedHistory = historyRepository.saveAndFlush(history);

        Path procPath = Files.createTempFile("import_proc_", "_" + originalFileName);
        try (InputStream in = storageService.getFile(objectName)) {
            Files.copy(in, procPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 3. Запускаем парсинг
        self.processFileAsync(procPath.toFile(), savedHistory.getId(), originalFileName);
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