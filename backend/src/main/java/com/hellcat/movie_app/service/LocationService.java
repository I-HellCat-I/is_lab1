package com.hellcat.movie_app.service;

import com.hellcat.movie_app.dto.LocationDto;
import com.hellcat.movie_app.entity.Location;
import com.hellcat.movie_app.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Location create(LocationDto dto) {
        Location location = new Location();
        location.setName(dto.getName());
        location.setX(dto.getX());
        location.setY(dto.getY());
        return locationRepository.save(location);
    }

    public Location update(Long id, LocationDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Локация не найдена")); // Лучше использовать EntityNotFoundException
        location.setName(dto.getName());
        location.setX(dto.getX());
        location.setY(dto.getY());
        return locationRepository.save(location);
    }

    public void delete(Long id) {
        locationRepository.deleteById(id);
    }
}