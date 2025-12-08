package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.dto.MovieDto;
import com.hellcat.movie_app.entity.Movie;
import com.hellcat.movie_app.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // GET /api/movies?page=0&size=10&sort=name,asc&nameFilter=Terminator
    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            // Принимаем 'filter' вместо 'nameFilter'
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(movieService.findAll(pageable, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody MovieDto movieDto) {
        Movie createdMovie = movieService.create(movieDto);
        return new ResponseEntity<>(createdMovie, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDto movieDto) {
        return ResponseEntity.ok(movieService.update(id, movieDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}