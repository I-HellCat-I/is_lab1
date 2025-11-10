package com.hellcat.backend.util;


import com.hellcat.backend.dto.LocationDTO;
import com.hellcat.backend.dto.MovieDTO;
import com.hellcat.backend.dto.PersonDTO;
import com.hellcat.backend.model.Coordinates;
import com.hellcat.backend.model.Location;
import com.hellcat.backend.model.Movie;
import com.hellcat.backend.model.Person;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.stream.Collectors;
import java.util.List;

@ApplicationScoped // Один на всё приложение.
public class EntityMapper {

    // --- Location ---
    public LocationDTO toDto(Location entity) {
        if (entity == null) return null;
        LocationDTO dto = new LocationDTO();
        dto.setId(entity.getId());
        dto.setX(entity.getX());
        dto.setY(entity.getY());
        dto.setName(entity.getName());
        return dto;
    }

    public Location toEntity(LocationDTO dto) {
        if (dto == null) return null;
        Location entity = new Location();
        entity.setId(dto.getId()); // ID может быть null при создании
        entity.setX(dto.getX());
        entity.setY(dto.getY());
        entity.setName(dto.getName());
        return entity;
    }

    // --- Person ---
    public PersonDTO toDto(Person entity) {
        if (entity == null) return null;
        PersonDTO dto = new PersonDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEyeColor(entity.getEyeColor());
        dto.setHairColor(entity.getHairColor());
        dto.setLocation(toDto(entity.getLocation())); // Рекурсивный вызов!
        dto.setWeight(entity.getWeight());
        dto.setNationality(entity.getNationality());
        return dto;
    }

    public Person toEntity(PersonDTO dto) {
        if (dto == null) return null;
        Person entity = new Person();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setEyeColor(dto.getEyeColor());
        entity.setHairColor(dto.getHairColor());
        entity.setLocation(toEntity(dto.getLocation())); // Рекурсивный вызов!
        entity.setWeight(dto.getWeight());
        entity.setNationality(dto.getNationality());
        return entity;
    }

    // --- Movie ---
    public MovieDTO toDto(Movie entity) {
        if (entity == null) return null;
        MovieDTO dto = new MovieDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getCoordinates() != null) {
            dto.setCoordinatesX(entity.getCoordinates().getX());
            dto.setCoordinatesY(entity.getCoordinates().getY());
        }
        dto.setCreationDate(entity.getCreationDate());
        dto.setOscarsCount(entity.getOscarsCount());
        dto.setBudget(entity.getBudget());
        dto.setTotalBoxOffice(entity.getTotalBoxOffice());
        dto.setMpaaRating(entity.getMpaaRating());
        dto.setDirector(toDto(entity.getDirector()));
        dto.setScreenwriter(toDto(entity.getScreenwriter()));
        dto.setOperator(toDto(entity.getOperator()));
        dto.setLength(entity.getLength());
        dto.setGoldenPalmCount(entity.getGoldenPalmCount());
        dto.setGenre(entity.getGenre());
        return dto;
    }

    public Movie toEntity(MovieDTO dto) {
        if (dto == null) return null;
        Movie entity = new Movie();
        // ID и creationDate мы не устанавливаем, они генерятся базой
        entity.setName(dto.getName());
        if (dto.getCoordinatesX() != null && dto.getCoordinatesY() != null) {
            Coordinates coords = new Coordinates();
            coords.setX(dto.getCoordinatesX());
            coords.setY(dto.getCoordinatesY());
            entity.setCoordinates(coords);
        }
        entity.setOscarsCount(dto.getOscarsCount());
        entity.setBudget(dto.getBudget());
        entity.setTotalBoxOffice(dto.getTotalBoxOffice());
        entity.setMpaaRating(dto.getMpaaRating());
        entity.setDirector(toEntity(dto.getDirector()));
        entity.setScreenwriter(toEntity(dto.getScreenwriter()));
        entity.setOperator(toEntity(dto.getOperator()));
        entity.setLength(dto.getLength());
        entity.setGoldenPalmCount(dto.getGoldenPalmCount());
        entity.setGenre(dto.getGenre());
        return entity;
    }

    public List<PersonDTO> toPersonDtoList(List<Person> persons) {
        if (persons == null) return null;
        return persons.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LocationDTO> toLocationDtoList(List<Location> locations) {
        if (locations == null) return null;
        return locations.stream().map(this::toDto).collect(Collectors.toList());
    }

    public LocationDTO toLocationDto(Location location) {
        return toDto(location);
    }

    public Location toLocation(LocationDTO dto) {
        return toEntity(dto);
    }

    // Утилитарный метод для маппинга списков фильмов
    public List<MovieDTO> toMovieDtoList(List<Movie> movies) {
        if (movies == null) return null;
        return movies.stream().map(this::toDto).collect(Collectors.toList());
    }
}
