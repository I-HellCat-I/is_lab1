package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.entity.MovieGenre;
import com.hellcat.movie_app.entity.Person;
import com.hellcat.movie_app.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies/special")
@RequiredArgsConstructor
public class MovieSpecialController {

    private final MovieService movieService;

    @GetMapping("/golden-palm-sum")
    public ResponseEntity<Map<String, Long>> getGoldenPalmSum() {
        return ResponseEntity.ok(Map.of("sum", movieService.getGoldenPalmCountSum()));
    }

    @GetMapping("/count-by-golden-palm")
    public ResponseEntity<Map<String, Long>> countByGoldenPalm(@RequestParam int count) {
        return ResponseEntity.ok(Map.of("count", movieService.countByGoldenPalmCount(count)));
    }

    @GetMapping("/count-less-than-golden-palm")
    public ResponseEntity<Map<String, Long>> countLessThanGoldenPalm(@RequestParam int count) {
        return ResponseEntity.ok(Map.of("count", movieService.countByGoldenPalmCountLessThan(count)));
    }

    @GetMapping("/screenwriters-no-oscars")
    public ResponseEntity<List<Person>> getScreenwritersWithNoOscars() {
        return ResponseEntity.ok(movieService.findScreenwritersWithNoOscars());
    }

    @PostMapping("/reset-oscars/{genre}")
    public ResponseEntity<Void> resetOscars(@PathVariable MovieGenre genre) {
        movieService.removeOscarsFromDirectorsOfGenre(genre);
        return ResponseEntity.ok().build();
    }
}