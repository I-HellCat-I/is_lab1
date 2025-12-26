package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.entity.ImportHistory;
import com.hellcat.movie_app.repository.ImportHistoryRepository;
import com.hellcat.movie_app.service.ImportService;
import com.hellcat.movie_app.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final ImportHistoryRepository historyRepository;
    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<String> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        if (files.length == 0) {
            return ResponseEntity.badRequest().body("No files selected");
        }
        importService.handleImport(files);
        return ResponseEntity.ok("Import started for " + files.length + " files");
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportHistory>> getHistory() {
        return ResponseEntity.ok(historyRepository.findAllByOrderByIdDesc());
    }

    @GetMapping("/{historyId}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long historyId) {
        ImportHistory history = historyRepository.findById(historyId).orElseThrow();
        InputStreamResource resource = new InputStreamResource(
                storageService.getFile(history.getMinioObjectName())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + history.getFileName() + "\"")
                .body(resource);
    }
}