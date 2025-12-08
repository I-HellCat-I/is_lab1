package com.hellcat.movie_app.exception;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(Map.of("error", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Общий обработчик для других непредвиденных ошибок
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        // Логирование исключения
        ex.printStackTrace();
        return new ResponseEntity<>(Map.of("error", "An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Превышен максимальный размер файла");
        response.put("message", "Максимальный размер файла - 100MB");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exc) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Некорректные параметры импорта");
        response.put("message", exc.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({OptimisticLockException.class, JpaOptimisticLockingFailureException.class})
    public ResponseEntity<Map<String, String>> handleOptimisticLocking(Exception ex) {
        return new ResponseEntity<>(
                Map.of("error", "Conflict: Data has been modified by another user. Please refresh and try again."),
                HttpStatus.CONFLICT // 409
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Нарушение целостности данных.";

        // Пытаемся достать полезное сообщение от базы данных
        // Обычно оно лежит глубоко в 'cause'
        if (ex.getRootCause() != null) {
            message = ex.getRootCause().getMessage();
        }

        return new ResponseEntity<>(
                Map.of(
                        "error", "Conflict",
                        "message", "Database constraint violation: " + message
                ),
                HttpStatus.CONFLICT // 409
        );
    }

    @ExceptionHandler(PersonAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handlePersonAlreadyExistsException(DataIntegrityViolationException ex) {
        String message = "Создаваемый вами человек уже существует.";

        // Пытаемся достать полезное сообщение от базы данных
        // Обычно оно лежит глубоко в 'cause'
        if (ex.getRootCause() != null) {
            message = ex.getRootCause().getMessage();
        }

        return new ResponseEntity<>(
                Map.of(
                        "error", "Conflict",
                        "message", "Database constraint violation: " + message
                ),
                HttpStatus.CONFLICT // 409
        );
    }
}