package com.pshs.ams.app.grade_levels.controllers;

import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.app.grade_levels.models.dto.GradeLevelDTO;
import com.pshs.ams.app.grade_levels.models.entities.GradeLevel;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.grade_levels.services.GradeLevelService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/grade-levels")
public class GradeLevelController {

	@Inject
	GradeLevelService gradeLevelService;
	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllGradeLevel(@BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		return Response.ok(
			gradeLevelService.getAllGradeLevel(Sort.by(sortRequest.sortBy, sortRequest.sortDirection), Page.of(pageRequest.page, pageRequest.size))
				.stream().map(gl -> mapper.map(gl, GradeLevelDTO.class)).toList()
		).build();
	}

	@GET
	@Path("/{id}")
	public Response getGradeLevelById(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		Optional<GradeLevel> gradeLevelOptional = gradeLevelService.getGradeLevelById(id);
		if (gradeLevelOptional.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Grade level not found", CodeStatus.NOT_FOUND)).build();
		}

		return Response.ok(mapper.map(gradeLevelOptional.get(), GradeLevelDTO.class)).build();
	}

	@POST
	@Path("/create")
	public Response createGradeLevel(GradeLevelDTO gradeLevelDTO) {
		if (gradeLevelDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Grade Level cannot be null", CodeStatus.BAD_REQUEST)).build();
		}

		// Convert DTO to Entity
		GradeLevel gradeLevel = mapper.map(gradeLevelDTO, GradeLevel.class);
		CodeStatus status = gradeLevelService.createGradeLevel(gradeLevel);
		return switch (status) {
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Grade Level cannot be null", CodeStatus.BAD_REQUEST)).build();
			case CONFLICT -> Response.ok(new MessageResponse("Grade level already exists", CodeStatus.CONFLICT)).build();
			case OK ->
				Response.status(Response.Status.OK).entity(new MessageResponse("Grade level created", CodeStatus.OK)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageResponse("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}

	@DELETE
	@Path("/{id}")
	public Response deleteGradeLevel(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		return switch (gradeLevelService.deleteGradeLevel(id)) {
			case OK -> Response.ok(new MessageResponse("Grade level deleted", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Grade level not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageResponse("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}

	@GET
	@Path("/name/{name}")
	public Response getGradeLevelByName(@PathParam("name") String name) {
		if (name.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Name cannot be empty", CodeStatus.BAD_REQUEST)).build();
		}

		Optional<GradeLevel> gradeLevelOptional = gradeLevelService.getGradeLevelByName(name);

		if (gradeLevelOptional.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Grade level not found", CodeStatus.NOT_FOUND)).build();
		}

		return Response.ok(mapper.map(gradeLevelOptional.get(), GradeLevelDTO.class)).build();
	}

	@GET
	@Path("/search/name/{name}")
	public Response searchGradeLevelByName(@PathParam("name") String name) {
		return Response.ok(gradeLevelService.searchGradeLevelByName(name).stream().map(gl -> mapper.map(gl, GradeLevelDTO.class)).toList()).build();
	}
}
