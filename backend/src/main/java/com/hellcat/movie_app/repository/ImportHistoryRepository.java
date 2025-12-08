package com.hellcat.movie_app.repository;

import com.hellcat.movie_app.entity.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    // Для отображения последних операций сверху
    List<ImportHistory> findAllByOrderByIdDesc();
}