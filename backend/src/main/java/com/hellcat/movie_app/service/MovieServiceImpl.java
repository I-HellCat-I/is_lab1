package com.hellcat.movie_app.service;

import com.hellcat.movie_app.config.MovieWebSocketHandler;
import com.hellcat.movie_app.dto.*;
import com.hellcat.movie_app.entity.*;
import com.hellcat.movie_app.exception.EntityNotFoundException;
import com.hellcat.movie_app.repository.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final MovieWebSocketHandler webSocketHandler; // Для WebSocket

    private final String WS_TOPIC = "/topic/movies";

    @Override
    public Page<Movie> findAll(Pageable pageable, String filter) {
        if (StringUtils.hasText(filter)) {
            return movieRepository.findByFilter(filter, pageable);
        }
        return movieRepository.findAll(pageable);
    }

    @Override
    public Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie with id " + id + " not found"));
    }

    @Override
    @Transactional
    public Movie create(MovieDto movieDto) {
        Movie movie = new Movie();
        mapDtoToEntity(movie, movieDto); // Используем маппер
        Movie savedMovie = movieRepository.save(movie);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                webSocketHandler.broadcast(new WebSocketMessage("CREATED", savedMovie));
            }
        });
        return savedMovie;
    }

    @Override
    @Transactional
    public Movie update(Long id, MovieDto movieDto) {
        Movie existingMovie = findById(id);
        mapDtoToEntity(existingMovie, movieDto); // Используем маппер
        Movie updatedMovie = movieRepository.save(existingMovie);
        if (movieDto.getVersion() != null) {
            existingMovie.setVersion(movieDto.getVersion());
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                webSocketHandler.broadcast(new WebSocketMessage("UPDATED", updatedMovie));
            }
        });
        return updatedMovie;
    }


    @Override
    @Transactional
    public void delete(Long id) {
        Movie movieToDelete = findById(id);
        movieRepository.deleteById(id);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                webSocketHandler.broadcast(new WebSocketMessage("DELETED", movieToDelete));
            }
        });
    }

    // --- Специальные операции ---

    @Override
    public Long getGoldenPalmCountSum() {
        return movieRepository.sumAllGoldenPalmCount();
    }

    @Override
    public long countByGoldenPalmCount(int count) {
        return movieRepository.countByGoldenPalmCount(count);
    }

    @Override
    public long countByGoldenPalmCountLessThan(int count) {
        return movieRepository.countByGoldenPalmCountLessThan(count);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Person> findScreenwritersWithNoOscars() {
        return movieRepository.findScreenwritersWithNoOscars();
    }

    @Override
    @Transactional
    public void removeOscarsFromDirectorsOfGenre(MovieGenre genre) {
        List<Person> directors = movieRepository.findDirectorsByGenre(genre);
        if (!directors.isEmpty()) {
            movieRepository.resetOscarsForDirectors(directors);
            webSocketHandler.broadcast(new WebSocketMessage("BATCH_UPDATE", "Oscars reset for directors of genre " + genre));
        }
    }

    // Обновляем mapDtoToEntity
    private void mapDtoToEntity(Movie movie, MovieDto dto) {
        movie.setName(dto.getName());
        movie.setOscarsCount(dto.getOscarsCount());
        movie.setBudget(dto.getBudget());
        movie.setTotalBoxOffice(dto.getTotalBoxOffice());
        movie.setMpaaRating(dto.getMpaaRating());
        movie.setLength(dto.getLength());
        movie.setGoldenPalmCount(dto.getGoldenPalmCount());
        movie.setGenre(dto.getGenre());
        movie.setVersion(dto.getVersion());

        // Обрабатываем координаты
        Coordinates coordinates;
        if (movie.getCoordinates() != null) {
            coordinates = movie.getCoordinates(); // Обновляем существующие
        } else {
            coordinates = new Coordinates(); // Создаем новые
        }
        coordinates.setX(dto.getCoordinatesX());
        coordinates.setY(dto.getCoordinatesY());
        movie.setCoordinates(coordinatesRepository.save(coordinates));

        // Обрабатываем персон
        if (dto.getDirector() != null && dto.getDirector().getId() != null) {
            movie.setDirector(personRepository.findById(dto.getDirector().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Режиссер не найден")));
        } else {
            movie.setDirector(null);
        }

        if (dto.getScreenwriter() != null && dto.getScreenwriter().getId() != null) {
            movie.setScreenwriter(personRepository.findById(dto.getScreenwriter().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Сценарист не найден")));
        }

        if (dto.getOperator() != null && dto.getOperator().getId() != null) {
            movie.setOperator(personRepository.findById(dto.getOperator().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Оператор не найден")));
        } else {
            movie.setOperator(null);
        }
    }

    @Data
    @AllArgsConstructor
    private static class WebSocketMessage {
        private String type; // Поле 'type' вместо 'action'
        private Object payload;
    }
}