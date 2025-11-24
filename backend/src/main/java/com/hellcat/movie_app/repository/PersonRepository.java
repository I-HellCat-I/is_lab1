package com.hellcat.movie_app.repository;

import com.hellcat.movie_app.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}