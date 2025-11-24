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
}