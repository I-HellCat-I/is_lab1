package com.hellcat.movie_app.repository;

import com.hellcat.movie_app.entity.Movie;
import com.hellcat.movie_app.entity.Person;
import com.hellcat.movie_app.entity.MovieGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Для фильтрации по имени (полное совпадение, без учета регистра)
    Page<Movie> findByNameContainingIgnoreCase(String name, Pageable pageable);


    @Query("SELECT m FROM Movie m LEFT JOIN m.director d " +
            "WHERE lower(m.name) LIKE lower(concat('%', :filter, '%')) " +
            "OR lower(d.name) LIKE lower(concat('%', :filter, '%'))")
    Page<Movie> findByFilter(@Param("filter") String filter, Pageable pageable);
    // --- Специальные операции ---

    // Рассчитать сумму значений поля goldenPalmCount для всех объектов.
    @Query("SELECT SUM(m.goldenPalmCount) FROM Movie m")
    Long sumAllGoldenPalmCount();

    // Вернуть количество объектов, значение поля goldenPalmCount которых равно заданному.
    long countByGoldenPalmCount(int count);

    // Вернуть количество объектов, значение поля goldenPalmCount которых меньше заданного.
    long countByGoldenPalmCountLessThan(int count);

    // Получить список сценаристов, ни один фильм которых не получил ни одного "Оскара".
    // Логика: выбираем всех сценаристов, у которых максимальное количество оскаров в их фильмах равно 0.
    @Query("SELECT m.screenwriter FROM Movie m GROUP BY m.screenwriter HAVING MAX(m.oscarsCount) = 0")
    List<Person> findScreenwritersWithNoOscars();

    // Найти всех режиссеров, снявших хотя бы один фильм в указанном жанре
    @Query("SELECT DISTINCT m.director FROM Movie m WHERE m.genre = :genre AND m.director IS NOT NULL")
    List<Person> findDirectorsByGenre(@Param("genre") MovieGenre genre);

    // Обнулить оскары для всех фильмов указанных режиссеров
    @Modifying // Эта аннотация нужна для UPDATE и DELETE запросов
    @Query("UPDATE Movie m SET m.oscarsCount = 0 WHERE m.director IN :directors")
    void resetOscarsForDirectors(@Param("directors") List<Person> directors);
}