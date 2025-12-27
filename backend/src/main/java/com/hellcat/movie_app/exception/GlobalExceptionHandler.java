package com.hellcat.movie_app.exception;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.exceptions.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.UnknownHostException;
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

    @ExceptionHandler(BadFileException.class)
    public ResponseEntity<Map<String, String>> handleBadFileException(BadFileException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("message", ex.getMessage());
        return ResponseEntity.unprocessableContent().body(response);
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<Map<String, String>> handleUnknownHostException(UnknownHostException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Ошибка коммуникации серверных компонентов, пожалуйста, обратитесь к администратору");
        response.put("message", ex.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(PersonAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handlePersonAlreadyExists(PersonAlreadyExistsException ex) {
        return new ResponseEntity<>(
                Map.of("error", ex.getMessage()),
                HttpStatus.CONFLICT // 409
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Database constraint violation";

        if (ex.getRootCause() != null) {
            message = ex.getRootCause().getMessage();
        }

        return new ResponseEntity<>(
                Map.of("error", "Conflict: " + message),
                HttpStatus.CONFLICT // 409
        );
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(TransactionSystemException ex) {
        Throwable cause = ex.getRootCause();

        if (cause instanceof OptimisticLockException || cause instanceof JpaOptimisticLockingFailureException) {
            return new ResponseEntity<>(
                    Map.of("error", "Conflict: Data has been modified by another user."),
                    HttpStatus.CONFLICT
            );
        }

        return new ResponseEntity<>(
                Map.of("error", cause.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler({OptimisticLockException.class, JpaOptimisticLockingFailureException.class}) // <-- Ловим оба типа
    public ResponseEntity<Map<String, String>> handleOptimisticLockException(Exception ex) { // <-- Аргумент Exception (общий предок)
        return new ResponseEntity<>(
                Map.of("error", "Conflict: Data has been modified by another user. Please refresh and try again."),
                HttpStatus.CONFLICT
        );
    }
}