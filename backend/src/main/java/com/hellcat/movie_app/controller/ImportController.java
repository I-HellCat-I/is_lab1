package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.entity.ImportHistory;
import com.hellcat.movie_app.repository.ImportHistoryRepository;
import com.hellcat.movie_app.service.ImportService;
import lombok.RequiredArgsConstructor;
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
}