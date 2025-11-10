package com.hellcat.backend.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "locations")
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue
    private Long id;
    private float x;
    private Double y; //Поле не может быть null
    private String name; //Длина строки не должна быть больше 824, Поле не может быть null
}