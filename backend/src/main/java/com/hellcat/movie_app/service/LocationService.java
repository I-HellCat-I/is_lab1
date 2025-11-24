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
}