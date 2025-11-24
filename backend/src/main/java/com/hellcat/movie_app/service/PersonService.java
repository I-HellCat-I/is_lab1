package com.hellcat.movie_app.service;

import com.hellcat.movie_app.dto.PersonDto;
import com.hellcat.movie_app.entity.Person;
import com.hellcat.movie_app.exception.EntityNotFoundException;
import com.hellcat.movie_app.repository.LocationRepository;
import com.hellcat.movie_app.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Person create(PersonDto dto) {
        Person person = new Person();
        person.setName(dto.getName());
        person.setEyeColor(dto.getEyeColor());
        person.setHairColor(dto.getHairColor());
        person.setHeight(dto.getHeight()); // Маппим height из DTO
        person.setNationality(dto.getNationality());

        locationRepository.findById(dto.getLocation().getId()).ifPresentOrElse(
                person::setLocation,
                () -> { throw new EntityNotFoundException("Локация с ID " + dto.getLocation().getId() + " не найдена."); }
        );

        return personRepository.save(person);
    }
}