package com.hellcat.movie_app.service;

import com.hellcat.movie_app.dto.PersonDto;
import com.hellcat.movie_app.entity.Person;
import com.hellcat.movie_app.exception.EntityNotFoundException;
import com.hellcat.movie_app.repository.LocationRepository;
import com.hellcat.movie_app.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
        // Torture
        if (personRepository.countByName(dto.getName()) > 0) {
            throw new RuntimeException("Person with this name already exists!");
        }
        try {
            Thread.sleep(200); // Имитация задержки (думаем...), чтобы расширить окно для гонки
        } catch (InterruptedException e) {}


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

    public Person update(Long id, PersonDto dto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Персона не найдена"));

        person.setName(dto.getName());
        person.setEyeColor(dto.getEyeColor());
        person.setHairColor(dto.getHairColor());
        person.setHeight(dto.getHeight());
        person.setNationality(dto.getNationality());

        // Обновляем связь с локацией
        var location = locationRepository.findById(dto.getLocation().getId())
                .orElseThrow(() -> new RuntimeException("Локация не найдена"));
        person.setLocation(location);

        return personRepository.save(person);
    }

    @Cacheable(value = "persons", key = "#id")
    public PersonDto findById(Long id) {
        Person person = personRepository.findById(id).orElseThrow();
        return mapToDto(person);
    }

    private PersonDto mapToDto(Person person) {
        PersonDto dto = new PersonDto();
        dto.setName(person.getName());
        dto.setEyeColor(person.getEyeColor());
        dto.setHairColor(person.getHairColor());
        dto.setHeight(person.getHeight());
        dto.setNationality(person.getNationality());

        if (person.getLocation() != null) {
            PersonDto.LocationRef locRef = new PersonDto.LocationRef();
            locRef.setId(person.getLocation().getId());
            dto.setLocation(locRef);
        }
        return dto;
    }

    public void delete(Long id) {
        personRepository.deleteById(id);
    }
}