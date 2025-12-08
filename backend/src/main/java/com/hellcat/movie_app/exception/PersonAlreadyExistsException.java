package com.hellcat.movie_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PersonAlreadyExistsException extends RuntimeException {
    public PersonAlreadyExistsException() {
    super("\"Person with this name already exists!\"");
}
}