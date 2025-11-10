package com.hellcat.backend.rest;

import com.hellcat.backend.dto.*;
import com.hellcat.backend.model.MovieGenre;
import jakarta.validation.Valid;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.hellcat.backend.service.MovieService;

import java.net.URI;
import java.util.List;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    @Inject
    private MovieService movieService;

    // TODO: Добавить пагинацию, фильтрацию и сортировку
    @GET
    public Response getAllMovies(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") String[] sort, // ?sort=name,asc&sort=id,desc
            @QueryParam("filter") String filter // ?filter=терминатор
    ) {
        // Простые проверки на адекватность
        if (page < 0) page = 0;
        if (size > 100) size = 100; // Ограничиваем максимальный размер страницы

        PageDTO<MovieDTO> moviePage = movieService.getAllMoviesPaginated(page, size, sort, filter);
        return Response.ok(moviePage).build();
    }

    @GET
    @Path("/{id}")
    public Response getMovieById(@PathParam("id") Long id) {
        MovieDTO movie = movieService.getMovieById(id);
        return Response.ok(movie).build();
    }

    @POST
    public Response createMovie(@Valid MovieDTO movieDTO) { // @Valid - для активации Bean Validation
        MovieDTO createdMovie = movieService.createMovie(movieDTO);
        return Response.created(URI.create("/api/movies/" + createdMovie.getId())).entity(createdMovie).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateMovie(@PathParam("id") Long id, @Valid MovieDTO movieDTO) {
        MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
        return Response.ok(updatedMovie).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMovie(@PathParam("id") Long id) {
        movieService.deleteMovie(id);
        return Response.noContent().build(); // 204 No Content - стандарт для успешного DELETE
    }

    // --- ЭНДПОИНТЫ ДЛЯ СПЕЦОПЕРАЦИЙ ---

    @POST
    @Path("/special/delete-by-palm")
    public Response deleteByGoldenPalm(@QueryParam("count") long count) {
        if (count <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Количество пальм должно быть больше 0").build();
        }
        movieService.executeDeleteByGoldenPalm(count);
        return Response.ok("Операция по удалению завершена.").build();
    }

    @GET
    @Path("/special/group-by-director")
    public Response groupByDirector() {
        List<DirectorGroupDTO> groups = movieService.executeGroupMoviesByDirector();
        return Response.ok(groups).build();
    }

    @GET
    @Path("/special/count-by-genre")
    public Response countByGenre(@QueryParam("genre") MovieGenre genre) {
        if (genre == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Необходимо указать параметр 'genre'").build();
        }
        CountDTO count = movieService.executeCountByGenre(genre.name());
        return Response.ok(count).build();
    }

    @GET
    @Path("/special/operators-without-oscars")
    public Response getOperatorsWithoutOscars() {
        List<OperatorDTO> operators = movieService.executeGetOperatorsWithoutOscars();
        return Response.ok(operators).build();
    }

    @POST
    @Path("/special/add-oscars")
    public Response addOscarsToLongFilms(@QueryParam("minLength") int minLength, @QueryParam("awardCount") long awardCount) {
        if (minLength <= 0 || awardCount <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Длительность и количество наград должны быть больше 0").build();
        }
        movieService.executeAddOscarsToLongFilms(minLength, awardCount);
        return Response.ok("Оскары успешно розданы стахановцам кинематографа.").build();
    }
}