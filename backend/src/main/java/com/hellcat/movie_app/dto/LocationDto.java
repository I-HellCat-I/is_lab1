package com.hellcat.movie_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDto {
    @NotBlank(message = "Название локации не может быть пустым.")
    private String name;
    @NotNull(message = "Координата X обязательна.")
    private Double x;
    @NotNull(message = "Координата Y обязательна.")
    private Float y;
}