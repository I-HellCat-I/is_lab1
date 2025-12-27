package com.hellcat.movie_app.service;

import com.hellcat.movie_app.dto.LocationDto;
import com.hellcat.movie_app.entity.Location;
import com.hellcat.movie_app.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Cacheable(value = "locations", key = "#id")
    @Transactional(readOnly = true)
    public LocationDto findById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found: " + id));
        return mapToDto(location);
    }

    private LocationDto mapToDto(Location loc) {
        LocationDto dto = new LocationDto();
        dto.setName(loc.getName());
        dto.setX(loc.getX());
        dto.setY(loc.getY());
        return dto;
    }
}