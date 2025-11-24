package com.hellcat.movie_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "coordinates")
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long x;

    @DecimalMin(value = "-114.0", message = "Value should be greater than -115")
    @Column(nullable = false)
    private double y; // Значение поля должно быть больше -115
}