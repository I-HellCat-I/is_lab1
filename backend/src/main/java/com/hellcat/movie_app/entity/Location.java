package com.hellcat.movie_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull // Поле не может быть null
    @Column(nullable = false)
    private Double x;

    @NotNull // Поле не может быть null
    @Column(nullable = false)
    private Float y;

    private String name; // Поле может быть null
}