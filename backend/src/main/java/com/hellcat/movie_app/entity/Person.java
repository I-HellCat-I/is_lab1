package com.hellcat.movie_app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank // Поле не может быть null, Строка не может быть пустой
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Color eyeColor; // Поле может быть null

    @Enumerated(EnumType.STRING)
    private Color hairColor; // Поле может быть null

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "location_id")
    private Location location; // Поле может быть null

    @Positive // Значение поля должно быть больше 0
    @Column(nullable = false)
    private float height;

    @Enumerated(EnumType.STRING)
    private Country nationality; // Поле может быть null
}
