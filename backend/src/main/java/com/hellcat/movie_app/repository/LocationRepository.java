package com.hellcat.movie_app.repository;

import com.hellcat.movie_app.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}