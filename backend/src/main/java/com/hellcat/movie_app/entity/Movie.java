package com.hellcat.movie_app.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id; // Значение поля должно быть больше 0, уникальным, генерироваться автоматически

    @NotBlank // Поле не может быть null, Строка не может быть пустой
    @Column(nullable = false)
    private String name;

    @NotNull // Поле не может быть null
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "coordinates_id", nullable = false)
    private Coordinates coordinates;

    @NotNull // Поле не может быть null
    @Column(nullable = false, updatable = false, name="creation_date")
    private LocalDateTime creationDate; // Значение этого поля должно генерироваться автоматически

    @Positive // Значение поля должно быть больше 0
    @Column(nullable = false, name="oscars_count")
    private long oscarsCount;

    @Positive // Значение поля должно быть больше 0
    private Double budget; // Поле может быть null

    @Positive // Значение поля должно быть больше 0
    @Column(nullable = false, name="total_box_office")
    private float totalBoxOffice;

    @Enumerated(EnumType.STRING)
    private MpaaRating mpaaRating; // Поле может быть null

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "director_id")
    private Person director; // Поле может быть null

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "screenwriter_id", nullable = false)
    private Person screenwriter;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "operator_id")
    private Person operator; // Поле может быть null

    @NotNull // Поле не может быть null
    @Positive // Значение поля должно быть больше 0
    @Column(nullable = false)
    private Integer length;

    @Positive // Значение поля должно быть больше 0
    @Column(nullable = false, name="golden_palm_count")
    private int goldenPalmCount;

    @Enumerated(EnumType.STRING)
    private MovieGenre genre; // Поле может быть null

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
}