package com.hellcat.movie_app.repository;

import com.hellcat.movie_app.entity.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {

    List<ImportHistory> findAllByOrderByIdDesc();

    // --- АТОМАРНОЕ ОБНОВЛЕНИЕ ---
    // Это позволяет воркерам обновлять счетчики, не перезатирая данные друг друга
    @Modifying
    @Transactional
    @Query("UPDATE ImportHistory h SET " +
            "h.addedCount = h.addedCount + :successDelta, " +
            "h.failedCount = h.failedCount + :failDelta, " +
            "h.status = 'PROCESSING', " +
            "h.logInfo = CONCAT(COALESCE(h.logInfo, ''), :logSuffix) " +
            "WHERE h.id = :id")
    void atomicUpdateProgress(@Param("id") Long id,
                              @Param("successDelta") int successDelta,
                              @Param("failDelta") int failDelta,
                              @Param("logSuffix") String logSuffix);

    // 2. Метод установки общего количества (вызывается Продюсером в конце чтения)
    @Modifying
    @Transactional
    @Query("UPDATE ImportHistory h SET h.expectedCount = :total WHERE h.id = :id")
    void setExpectedCount(@Param("id") Long id, @Param("total") int total);
}