package com.hellcat.backend.rest;

import com.hellcat.backend.model.MovieGenre;
import com.hellcat.backend.model.MpaaRating;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.stream.Stream;

@Path("/meta")
@Produces(MediaType.APPLICATION_JSON)
public class MetaResource {

    @GET
    @Path("/enums")
    public Response getEnums() {
        Map<String, Object> enums = Map.of(
                "mpaaRatings", Stream.of(MpaaRating.values()).map(Enum::name).toArray(),
                "movieGenres", Stream.of(MovieGenre.values()).map(Enum::name).toArray()
                //... другие enums, если нужно
        );
        return Response.ok(enums).build();
    }
}