package com.hellcat.movie_app.dto;

import com.hellcat.movie_app.entity.Color;
import com.hellcat.movie_app.entity.Country;
import com.hellcat.movie_app.entity.MovieGenre;
import com.hellcat.movie_app.entity.MpaaRating;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MetaEnumsDto {
    private List<MpaaRating> mpaaRatings;
    private List<MovieGenre> movieGenres;
    private List<Color> colors;
    private List<Country> nationalities;
}