package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.dto.LocationDto;
import com.hellcat.movie_app.entity.Location;
import com.hellcat.movie_app.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<Location>> getAll() {
        return ResponseEntity.ok(locationService.findAll());
    }

    @PostMapping
    public ResponseEntity<Location> create(@Valid @RequestBody LocationDto dto) {
        return new ResponseEntity<>(locationService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> update(@PathVariable Long id, @Valid @RequestBody LocationDto dto) {
        // Простейшая реализация: ищем, обновляем поля, сохраняем
        return ResponseEntity.ok(locationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.findById(id));
    }
}