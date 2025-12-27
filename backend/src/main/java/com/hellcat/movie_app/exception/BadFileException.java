package com.hellcat.movie_app.exception;

public class BadFileException extends RuntimeException {
    public BadFileException(String message) {
        super(message);
    }
    public BadFileException() {
        super("Не удалось подготовить файл к импорту");
    }
}
