package com.hellcat.movie_app.service;

import com.hellcat.movie_app.config.MovieWebSocketHandler;
import com.hellcat.movie_app.config.RabbitConfig;
import com.hellcat.movie_app.config.WebSocketMessage;
import com.hellcat.movie_app.dto.ImportTaskDto;
import com.hellcat.movie_app.dto.MovieDto;
import com.hellcat.movie_app.entity.ImportHistory;
import com.hellcat.movie_app.repository.ImportHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportConsumer {

    private final MovieService movieService;
    private final ImportHistoryRepository historyRepository;
    private final MovieWebSocketHandler webSocketHandler;

    @RabbitListener(queues = RabbitConfig.IMPORT_QUEUE, concurrency = "1-20")
    public void consumeBatch(ImportTaskDto task) {
        int success = 0;
        int fail = 0;

        List<MovieDto> batch = task.getMovies();
        if (batch == null || batch.isEmpty()) return;


        try { Thread.sleep(50); } catch (InterruptedException e) {}

        for (MovieDto dto : batch) {
            try {
                movieService.create(dto);
                success++;
            } catch (Exception e) {
                fail++;
            }
        }

        // Обновляем прогресс и проверяем финиш
        checkCompletion(task.getHistoryId(), success, fail);
    }

    @Transactional
    public void checkCompletion(Long historyId, int success, int fail) {
        // Добавляем лог про поток, чтобы видеть мультипоточность
        String threadLog = String.format(" [Worker-%d processed %d] ", Thread.currentThread().getId(), success + fail);

        // 1. Атомарно обновляем счетчики
        historyRepository.atomicUpdateProgress(historyId, success, fail, threadLog);

        // 2. Проверяем, не всё ли готово
        // Важно: достаем свежую версию после update
        ImportHistory h = historyRepository.findById(historyId).orElse(null);

        if (h != null && h.getExpectedCount() != null) {
            int totalProcessed = h.getAddedCount() + h.getFailedCount();

            // Если обработали всё, что ожидали -> SUCCESS
            if (totalProcessed >= h.getExpectedCount()) {
                // Двойная проверка статуса, чтобы не спамить логами
                if (!"SUCCESS".equals(h.getStatus())) {
                    h.setStatus("SUCCESS");
                    h.setLogInfo(h.getLogInfo() + "\nDONE! All workers finished.");
                    historyRepository.save(h);

                    // Уведомляем фронт о победе
                    webSocketHandler.broadcast(new WebSocketMessage("BULK_UPDATE", "Import Complete!"));
                }
            }
        }
    }
}