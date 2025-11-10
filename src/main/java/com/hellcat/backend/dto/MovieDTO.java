package com.hellcat.backend.dto;

import com.hellcat.backend.model.MovieGenre;
import com.hellcat.backend.model.MpaaRating;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class MovieDTO {
    private Long id;
    private String name;
    private Double coordinatesX;
    private Float coordinatesY;
    private Date creationDate;
    private Long oscarsCount;
    private Double budget;
    private Integer totalBoxOffice;
    private MpaaRating mpaaRating;
    private PersonDTO director;
    private PersonDTO screenwriter;
    private PersonDTO operator;
    private Integer length;
    private Long goldenPalmCount;
    private MovieGenre genre;
}