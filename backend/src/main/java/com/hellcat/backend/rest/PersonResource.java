package com.hellcat.backend.rest;

import com.hellcat.backend.dto.PersonDTO;
import com.hellcat.backend.repository.PersonRepository;
import com.hellcat.backend.util.EntityMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    private PersonRepository personRepository;

    @Inject
    private EntityMapper mapper;

    /**
     * Возвращает полный список всех персон (режиссеров, сценаристов и т.д.),
     * существующих в системе, для использования в выпадающих списках на клиенте.
     * @return Response, содержащий список PersonDTO.
     */
    @GET
    public Response getAllPersons() {
        List<PersonDTO> personDTOs = mapper.toPersonDtoList(personRepository.findAll());
        return Response.ok(personDTOs).build();
    }
}