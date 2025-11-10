package com.hellcat.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data // Генерирует геттеры, сеттеры, equals, hashCode, toString
@NoArgsConstructor // Генерирует пустой конструктор
@Entity
@Table(name = "movies")
public class Movie {
    // ... (поля как в ТЗ)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded // Встраиваем координаты прямо в таблицу
    private Coordinates coordinates;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(nullable = false)
    private Long oscarsCount;

    private Double budget;

    @Column(nullable = false)
    private Integer totalBoxOffice;

    @Enumerated(EnumType.STRING)
    private MpaaRating mpaaRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Person director;

    @ManyToOne(fetch = FetchType.EAGER) // Сценарист важен, грузим сразу
    @JoinColumn(name = "screenwriter_id", nullable = false)
    private Person screenwriter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Person operator;

    @Column(nullable = false)
    private Integer length;

    @Column(nullable = false)
    private Long goldenPalmCount;

    @Enumerated(EnumType.STRING)
    private MovieGenre genre;

    @PrePersist // Этот метод будет вызван перед сохранением
    protected void onCreate() {
        creationDate = new Date();
    }
}