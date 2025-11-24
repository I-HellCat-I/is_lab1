package com.hellcat.movie_app.dto;

import com.hellcat.movie_app.entity.Color;
import com.hellcat.movie_app.entity.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PersonDto {
    @NotBlank
    private String name;
    @NotNull
    private Color eyeColor;
    @NotNull
    private Color hairColor;
    @NotNull
    @Positive(message = "Вес должен быть положительным числом.")
    // Фронтенд шлет 'weight', но наша сущность имеет 'height'.
    // Мы назовем поле 'height', чтобы Jackson правильно его распознал
    // при маппинге в сущность Person. На фронте это поле называется 'weight'.
    private Float height;
    @NotNull
    private Country nationality;
    @NotNull
    private LocationRef location;

    // Вложенный класс для ссылки на локацию, как этого ожидает фронтенд
    @Data
    public static class LocationRef {
        @NotNull
        private Long id;
    }
}