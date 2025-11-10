package com.hellcat.backend.rest;

import com.hellcat.backend.dto.PersonDTO;
import com.hellcat.backend.service.PersonService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @Inject
    private PersonService personService;

    @GET
    public Response getAllPersons() {
        List<PersonDTO> personDTOs = personService.getAllPersons();
        return Response.ok(personDTOs).build();
    }

    @POST
    public Response createPerson(PersonDTO personDTO) {
        PersonDTO createdPerson = personService.createPerson(personDTO);
        return Response.created(URI.create("/api/persons/" + createdPerson.getId()))
                .entity(createdPerson)
                .build();
    }
}