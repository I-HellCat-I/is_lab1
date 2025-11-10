package com.hellcat.backend.rest;

import com.hellcat.backend.dto.LocationDTO;
import com.hellcat.backend.repository.LocationRepository;
import com.hellcat.backend.util.EntityMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/locations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocationResource {

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private EntityMapper mapper;

    @GET
    public Response getAllLocations() {
        List<LocationDTO> locationDTOs = mapper.toLocationDtoList(locationRepository.findAll());
        return Response.ok(locationDTOs).build();
    }

    @POST
    public Response createLocation(LocationDTO locationDTO) {
        if (locationDTO.getName() == null || locationDTO.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Название локации не может быть пустым")
                    .build();
        }
        if (locationDTO.getName().length() > 824) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Название локации не может быть длиннее 824 символов")
                    .build();
        }

        LocationDTO createdLocation = mapper.toLocationDto(locationRepository.save(mapper.toLocation(locationDTO)));
        return Response.created(URI.create("/api/locations/" + createdLocation.getId()))
                .entity(createdLocation)
                .build();
    }
}
