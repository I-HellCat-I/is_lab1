package com.hellcat.movie_app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordinatesDto {
    @NotNull
    private Long x;

    @NotNull
    @DecimalMin(value = "-114.0", message = "Value should be greater than -115")
    private Double y;
}