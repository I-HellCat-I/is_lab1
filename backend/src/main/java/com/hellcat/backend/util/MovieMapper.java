package com.hellcat.backend.util;

import com.hellcat.backend.dto.MovieDTO;
import com.hellcat.backend.dto.PersonDTO;
import com.hellcat.backend.model.Movie;
import com.hellcat.backend.model.Person;
import jakarta.enterprise.context.ApplicationScoped;


import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped // Это наш специалист-переводчик
public class MovieMapper {

    // --- Entity в DTO ---

    public PersonDTO toDto(Person person) {
        if (person == null) return null;
        PersonDTO dto = new PersonDTO();
        dto.setId(person.getId());
        dto.setName(person.getName());
        dto.setEyeColor(person.getEyeColor());
        dto.setHairColor(person.getHairColor());
        dto.setWeight(person.getWeight());
        dto.setNationality(person.getNationality());
        if (person.getLocation() != null) {
            dto.setLocationName(person.getLocation().getName());
        }
        return dto;
    }

    public MovieDTO toDto(Movie movie) {
        if (movie == null) return null;
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setName(movie.getName());
        if (movie.getCoordinates() != null) {
            dto.setCoordinatesX(movie.getCoordinates().getX());
            dto.setCoordinatesY(movie.getCoordinates().getY());
        }
        dto.setCreationDate(movie.getCreationDate());
        dto.setOscarsCount(movie.getOscarsCount());
        dto.setBudget(movie.getBudget());
        dto.setTotalBoxOffice(movie.getTotalBoxOffice());
        dto.setMpaaRating(movie.getMpaaRating());
        dto.setLength(movie.getLength());
        dto.setGoldenPalmCount(movie.getGoldenPalmCount());
        dto.setGenre(movie.getGenre());

        // Маппим связанных личностей
        dto.setDirector(toDto(movie.getDirector()));
        dto.setScreenwriter(toDto(movie.getScreenwriter()));
        dto.setOperator(toDto(movie.getOperator()));

        return dto;
    }

    public List<MovieDTO> toDto(List<Movie> movies) {
        return movies.stream().map(this::toDto).collect(Collectors.toList());
    }

    // --- DTO в Entity ---
    // В этой лабе нам это нужно только для создания/обновления Movie.
    // Мы не будем создавать Person через MovieDTO, а будем подтягивать существующих по ID.
    // Поэтому маппер из DTO в Entity будет проще.

    public void updateEntityFromDto(Movie movie, MovieDTO dto) {
        // Этот метод будет обновлять существующую сущность `movie` данными из `dto`
        // ... (movie.setName(dto.getName()), etc.)
        // Здесь же будет логика по поиску Person по ID, если DTO передает только ID
    }
}
