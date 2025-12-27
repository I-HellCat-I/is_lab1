package com.hellcat.movie_app.exception;

public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException(String message) {
        super(message);
    }
    public PersonAlreadyExistsException() {
        super("Person with this name already exists!");
    }
}
