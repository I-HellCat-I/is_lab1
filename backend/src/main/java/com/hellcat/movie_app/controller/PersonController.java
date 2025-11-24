package com.hellcat.movie_app.controller;

import com.hellcat.movie_app.dto.PersonDto;
import com.hellcat.movie_app.entity.Person;
import com.hellcat.movie_app.service.PersonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    private final PersonService personService;

    @GetMapping
    public ResponseEntity<List<Person>> getAll() {
        return ResponseEntity.ok(personService.findAll());
    }

    @PostMapping
    public ResponseEntity<Person> create(@Valid @RequestBody PersonDto dto) {
        return new ResponseEntity<>(personService.create(dto), HttpStatus.CREATED);
    }
}