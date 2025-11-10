package com.hellcat.backend.service;

import com.hellcat.backend.dto.*;
import com.hellcat.backend.model.Coordinates;
import com.hellcat.backend.model.Person;
import com.hellcat.backend.repository.PersonRepository;
import com.hellcat.backend.util.EntityMapper;
import com.hellcat.backend.websocket.NotificationSocket;
import com.hellcat.backend.dto.WebSocketMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import com.hellcat.backend.model.Movie;
import com.hellcat.backend.repository.MovieRepository;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MovieService {

    @Inject private MovieRepository movieRepository;
    @Inject private PersonRepository personRepository;
    @Inject private EntityMapper mapper;
    @Inject private NotificationSocket notificationSocket;

    public List<MovieDTO> getAllMovies() {
        return mapper.toMovieDtoList(movieRepository.findAll());
    }

    public MovieDTO getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO dto) {
        Movie movie = mapper.toEntity(dto);

        // Связываем с существующими персонами из БД по ID
        movie.setDirector(findPersonOrThrow(dto.getDirector() != null ? dto.getDirector().getId() : null, "Режиссер"));
        movie.setScreenwriter(findPersonOrThrow(dto.getScreenwriter() != null ? dto.getScreenwriter().getId() : null, "Сценарист"));
        movie.setOperator(findPersonOrThrow(dto.getOperator() != null ? dto.getOperator().getId() : null, "Оператор"));

        Movie savedMovie = movieRepository.save(movie);
        MovieDTO resultDto = mapper.toDto(savedMovie);

        // Отправляем объект WebSocketMessage с типом CREATED и полным DTO фильма
        notificationSocket.broadcast(new WebSocketMessage<>(WebSocketMessage.MessageType.CREATED, resultDto));

        return resultDto;
    }

    @Transactional
    public MovieDTO updateMovie(Long id, MovieDTO dto) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " для обновления не найден"));

        // Обновляем поля из DTO
        existingMovie.setName(dto.getName());
        Coordinates coords = new Coordinates();
        coords.setX(dto.getCoordinatesX());
        coords.setY(dto.getCoordinatesY());
        existingMovie.setCoordinates(coords);
        existingMovie.setOscarsCount(dto.getOscarsCount());
        existingMovie.setBudget(dto.getBudget());
        existingMovie.setTotalBoxOffice(dto.getTotalBoxOffice());
        existingMovie.setMpaaRating(dto.getMpaaRating());
        existingMovie.setLength(dto.getLength());
        existingMovie.setGoldenPalmCount(dto.getGoldenPalmCount());
        existingMovie.setGenre(dto.getGenre());

        // Обновляем связи
        existingMovie.setDirector(findPersonOrThrow(dto.getDirector() != null ? dto.getDirector().getId() : null, "Режиссер"));
        existingMovie.setScreenwriter(findPersonOrThrow(dto.getScreenwriter() != null ? dto.getScreenwriter().getId() : null, "Сценарист"));
        existingMovie.setOperator(findPersonOrThrow(dto.getOperator() != null ? dto.getOperator().getId() : null, "Оператор"));

        Movie updatedMovie = movieRepository.save(existingMovie);
        MovieDTO resultDto = mapper.toDto(updatedMovie);

        // Отправляем объект WebSocketMessage с типом UPDATED и полным DTO фильма
        notificationSocket.broadcast(new WebSocketMessage<>(WebSocketMessage.MessageType.UPDATED, resultDto));

        return resultDto;
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (movieRepository.deleteById(id)) {
            // Для удаления мы можем создать специальный payload или просто DTO с одним ID.
            // Здесь для простоты создадим объект, содержащий только ID.
            var payload = new java.util.HashMap<String, Long>();
            payload.put("id", id);
            notificationSocket.broadcast(new WebSocketMessage<>(WebSocketMessage.MessageType.DELETED, payload)); // Предполагаем, что есть тип DELETED
        } else {
            throw new NotFoundException("Фильм с ID " + id + " для удаления не найден");
        }
    }

    private Person findPersonOrThrow(Integer personId, String role) {
        if (personId == null) {
            return null; // Разрешаем null для режиссера и оператора
        }
        return personRepository.findById(personId)
                .orElseThrow(() -> new BadRequestException(role + " с ID " + personId + " не найден в базе"));
    }

    // --- РЕАЛИЗАЦИЯ СПЕЦОПЕРАЦИЙ ---

    @Transactional
    public void executeDeleteByGoldenPalm(long palmCount) {
        movieRepository.deleteByGoldenPalm(palmCount);
        notificationSocket.broadcast(new WebSocketMessage<>(WebSocketMessage.MessageType.BULK_DELETE, "palmCount")); // Сигнал о массовом изменении
    }

    public List<DirectorGroupDTO> executeGroupMoviesByDirector() {
        return movieRepository.groupMoviesByDirector().stream()
                .map(row -> new DirectorGroupDTO((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    public CountDTO executeCountByGenre(String genre) {
        Long count = movieRepository.countByGenre(genre.toUpperCase());
        return new CountDTO(count);
    }

    public List<OperatorDTO> executeGetOperatorsWithoutOscars() {
        return movieRepository.getOperatorsWithoutOscars().stream()
                .map(row -> new OperatorDTO(((Number) row[0]).intValue(), (String) row[1]))
                .collect(Collectors.toList());
    }

    @Transactional
    public void executeAddOscarsToLongFilms(int minLength, long awardCount) {
        movieRepository.addOscarsToLongFilms(minLength, awardCount);
        notificationSocket.broadcast(new WebSocketMessage<>(WebSocketMessage.MessageType.BULK_UPDATE, "addOscars"));
    }

    public PageDTO<MovieDTO> getAllMoviesPaginated(int page, int size, String[] sort, String filter) {
        List<Movie> movies = movieRepository.findByCriteria(page, size, sort, filter);
        long totalElements = movieRepository.countByCriteria(filter);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        List<MovieDTO> movieDTOs = mapper.toMovieDtoList(movies);
        return new PageDTO<>(movieDTOs, page, size, totalElements, totalPages);
    }
}