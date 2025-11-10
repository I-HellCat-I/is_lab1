package com.hellcat.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Embeddable // Этот класс будет встраиваться в другие таблицы
public class Coordinates {
    @Column(name = "coordinates_x")
    private Double x;
    @Column(name = "coordinates_y")
    private Float y;
}