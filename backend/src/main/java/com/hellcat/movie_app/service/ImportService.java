package com.hellcat.movie_app.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellcat.movie_app.config.MovieWebSocketHandler;
import com.hellcat.movie_app.config.RabbitConfig;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportHistoryRepository historyRepository;
    private final RabbitTemplate rabbitTemplate; // <-- Кролик
    private final MovieWebSocketHandler webSocketHandler;

    @Lazy
    @Autowired
    private ImportService self;

    private final JsonFactory jsonFactory = new JsonFactory();
    private YAMLFactory yamlFactory;

    {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setCodePointLimit(500 * 1024 * 1024);
        yamlFactory = YAMLFactory.builder().loaderOptions(loaderOptions).build();
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // handleImport остается таким же (копирование во временный файл)
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
        ImportHistory history = new ImportHistory();
        history.setFileName(originalFileName);
        history.setStatus("QUEUED"); // Статус: В очереди
        history.setLogInfo("Reading file and sending to RabbitMQ...\n");
        history = historyRepository.save(history);

        int batchSize = 200; // Можно больше, т.к. Rabbit переварит
        long startTime = System.currentTimeMillis();

        try (InputStream inputStream = new FileInputStream(fileOnDisk)) {
            JsonFactory factory = (originalFileName.endsWith(".yaml") || originalFileName.endsWith(".yml"))
                    ? yamlFactory : jsonFactory;

            try (JsonParser parser = factory.createParser(inputStream)) {
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new IllegalStateException("Expected content to be an array");
                }

                List<MovieDto> batch = new ArrayList<>();
                int totalSent = 0;

                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    MovieDto dto = mapper.readValue(parser, MovieDto.class);
                    batch.add(dto);

                    if (batch.size() >= batchSize) {
                        // Отправляем батч в очередь
                        sendBatchToRabbit(history.getId(), originalFileName, batch);
                        totalSent += batch.size();
                        batch.clear();
                    }
                }

                if (!batch.isEmpty()) {
                    sendBatchToRabbit(history.getId(), originalFileName, batch);
                    totalSent += batch.size();
                }

                historyRepository.setExpectedCount(history.getId(), totalSent);

                String msg = String.format("Sent %d tasks to Queue. Workers are processing...\n", totalSent);
                self.updateLogSafe(history.getId(), msg);
            }
        } catch (Exception e) {
            log.error("Error reading file", e);
            history.setStatus("FAILED");
            self.updateLogSafe(history.getId(), "Error: " + e.getMessage());
            historyRepository.save(history); // Сохраняем статус ошибки
        } finally {
            if (fileOnDisk.exists()) fileOnDisk.delete();
        }
    }

    private void sendBatchToRabbit(Long historyId, String fileName, List<MovieDto> batch) {
        // Создаем копию листа, чтобы не было проблем с очисткой
        ImportTaskDto task = new ImportTaskDto(historyId, fileName, new ArrayList<>(batch));
        rabbitTemplate.convertAndSend(RabbitConfig.IMPORT_QUEUE, task);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLogSafe(Long historyId, String message) {
        ImportHistory h = historyRepository.findById(historyId).orElse(null);
        if (h != null) {
            String current = h.getLogInfo() == null ? "" : h.getLogInfo();
            if (current.length() > 50000) current = current.substring(0, 49000) + "\n...truncated...";
            h.setLogInfo(current + message);
            historyRepository.saveAndFlush(h);
        }
    }
}