package com.hellcat.backend.service;

import com.hellcat.backend.dto.PersonDTO;
import com.hellcat.backend.model.Person;
import com.hellcat.backend.repository.LocationRepository;
import com.hellcat.backend.repository.PersonRepository;
import com.hellcat.backend.util.EntityMapper;
import com.hellcat.backend.websocket.NotificationSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class PersonService {

    @Inject
    private PersonRepository personRepository;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private EntityMapper mapper;

    @Inject
    private NotificationSocket notificationSocket;

    public List<PersonDTO> getAllPersons() {
        return mapper.toPersonDtoList(personRepository.findAll());
    }

    public PersonDTO createPerson(PersonDTO personDTO) {
        validatePersonDTO(personDTO);

        Person person = mapper.toEntity(personDTO);

        if (personDTO.getLocation() != null && personDTO.getLocation().getId() != null) {
            var location = locationRepository.findById(personDTO.getLocation().getId())
                    .orElseThrow(() -> new WebApplicationException(
                            Response.status(Response.Status.BAD_REQUEST)
                                    .entity("Локация с ID " + personDTO.getLocation().getId() + " не найдена")
                                    .build()
                    ));
            person.setLocation(location);
        } else {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Необходимо выбрать локацию для персоны")
                            .build()
            );
        }

        Person savedPerson = personRepository.save(person);
        PersonDTO result = mapper.toDto(savedPerson);

        notificationSocket.broadcast("PERSON_CREATED", result);

        return result;
    }

    private void validatePersonDTO(PersonDTO personDTO) {
        if (personDTO.getName() == null || personDTO.getName().trim().isEmpty()) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Имя персоны не может быть пустым")
                            .build()
            );
        }
        if (personDTO.getEyeColor() == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Необходимо указать цвет глаз")
                            .build()
            );
        }
        if (personDTO.getHairColor() == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Необходимо указать цвет волос")
                            .build()
            );
        }
        if (personDTO.getWeight() == null || personDTO.getWeight() <= 0) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Вес должен быть положительным числом")
                            .build()
            );
        }
    }
}
