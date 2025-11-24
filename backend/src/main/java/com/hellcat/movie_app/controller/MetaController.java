package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.dto.MetaEnumsDto;
import com.hellcat.movie_app.entity.Color;
import com.hellcat.movie_app.entity.Country;
import com.hellcat.movie_app.entity.MovieGenre;
import com.hellcat.movie_app.entity.MpaaRating;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    @GetMapping("/enums")
    public ResponseEntity<MetaEnumsDto> getAllEnums() {
        MetaEnumsDto enums = new MetaEnumsDto(
                Arrays.asList(MpaaRating.values()),
                Arrays.asList(MovieGenre.values()),
                Arrays.asList(Color.values()),
                Arrays.asList(Country.values())
        );
        return ResponseEntity.ok(enums);
    }
}