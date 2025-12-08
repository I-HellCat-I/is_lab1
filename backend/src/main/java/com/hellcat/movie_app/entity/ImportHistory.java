package com.hellcat.movie_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Data
public class ImportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;
    private String status; // STARTED, SUCCESS, FAILED
    @Column(name = "added_count")
    private int addedCount;
    @Column(name = "failed_count")
    private int failedCount;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT", name = "log_info")
    private String logInfo;

    @PrePersist
    public void onCreate() {
        this.startTime = LocalDateTime.now();
    }
}