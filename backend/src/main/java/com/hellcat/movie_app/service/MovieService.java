package com.hellcat.movie_app.service;

import com.hellcat.movie_app.dto.MovieDto;
import com.hellcat.movie_app.entity.Movie;
import com.hellcat.movie_app.entity.MovieGenre;
import com.hellcat.movie_app.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    Page<Movie> findAll(Pageable pageable, String nameFilter);
    Movie findById(Long id);
    Movie create(MovieDto movieDto);
    Movie update(Long id, MovieDto movieDto);
    void delete(Long id);

    // Специальные операции
    Long getGoldenPalmCountSum();
    long countByGoldenPalmCount(int count);
    long countByGoldenPalmCountLessThan(int count);
    List<Person> findScreenwritersWithNoOscars();
    void removeOscarsFromDirectorsOfGenre(MovieGenre genre);
}