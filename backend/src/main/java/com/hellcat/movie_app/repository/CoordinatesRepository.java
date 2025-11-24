package com.hellcat.movie_app.repository;

import com.hellcat.movie_app.entity.Coordinates;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordinatesRepository extends JpaRepository<Coordinates, Long> {
}