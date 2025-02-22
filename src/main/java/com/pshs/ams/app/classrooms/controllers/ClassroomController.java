package com.pshs.ams.app.classrooms.controllers;

import com.pshs.ams.app.classrooms.models.dto.ClassroomDTO;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.classrooms.services.ClassroomService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/classrooms")
public class ClassroomController {

	@Inject
	ClassroomService classroomService;

	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllClassrooms(@BeanParam SortRequest sort, @BeanParam PageRequest page) {
		return Response.ok(
				classroomService.getAllClasses(
						Sort.by(sort.sortBy, sort.sortDirection),
						Page.of(page.page, page.size)).stream().map(classroom -> mapper.map(classroom, ClassroomDTO.class))
					.toList())
			.build();
	}

	@PUT
	@Path("/update")
	public Response updateClassroom(ClassroomDTO classroomDTO) {
		if (classroomDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom cannot be null",
						CodeStatus.BAD_REQUEST))
				.build();
		}

		CodeStatus status = this.classroomService.updateClass(
			mapper.map(classroomDTO, Classroom.class));

		return switch (status) {
			case OK -> Response.ok(
					new MessageResponse(
						"Classroom updated",
						CodeStatus.OK))
				.build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
					new MessageResponse(
						"Internal Server Error",
						CodeStatus.FAILED))
				.build();
		};
	}

	@POST
	@Path("/create")
	public Response createClassroom(ClassroomDTO classroomDTO) {
		if (classroomDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"",
						CodeStatus.BAD_REQUEST))
				.build();
		}

		// Convert the dto to entity
		Classroom classroom = mapper.map(classroomDTO, Classroom.class);
		Instant now = Instant.now();
		classroom.setCreatedAt(now);
		classroom.setUpdatedAt(now);

		CodeStatus status = classroomService.createClass(classroom);

		return switch (status) {
			case OK -> Response.ok(
					new MessageResponse(
						"Classroom created",
						CodeStatus.OK))
				.build();
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom cannot be null",
						CodeStatus.BAD_REQUEST))
				.build();
			case EXISTS -> Response.ok(
					new MessageResponse(
						"Classroom already exists",
						CodeStatus.EXISTS))
				.build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
					new MessageResponse(
						"Internal Server Error",
						CodeStatus.FAILED))
				.build();
		};
	}

	@DELETE
	@Path("/{id}")
	public Response deleteClassroom(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom id cannot be less than or equal to zero",
						CodeStatus.BAD_REQUEST))
				.build();
		}

		return switch (classroomService.deleteClassroom(id)) {
			case OK -> Response.ok(
					new MessageResponse(
						"Classroom deleted",
						CodeStatus.OK))
				.build();
			case NOT_FOUND -> Response.status(Response.Status.NOT_FOUND).entity(
					new MessageResponse(
						"Classroom not found",
						CodeStatus.NOT_FOUND))
				.build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
					new MessageResponse(
						"Internal Server Error",
						CodeStatus.FAILED))
				.build();
		};
	}

	@GET
	@Path("/search/name/{name}")
	public Response searchClassroomByName(@PathParam("name") String name, @BeanParam PageRequest pageRequest,
	                                      @BeanParam SortRequest sortRequest) {
		if (name.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Name cannot be empty",
						CodeStatus.BAD_REQUEST))
				.build();
		}

		return Response.ok(
				classroomService
					.searchClassroomByName(name, Page.of(pageRequest.page, pageRequest.size),
						Sort.by(sortRequest.sortBy, sortRequest.sortDirection))
					.stream().map(cls -> mapper.map(cls, ClassroomDTO.class)).toList())
			.build();
	}

	@GET
	@Path("/{id}")
	public Response getClassroom(@PathParam("id") Long id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom id cannot be less than or equal to zero",
						CodeStatus.BAD_REQUEST))
				.build();
		}
		Optional<Classroom> classroom = classroomService.getClassroom(id);
		return classroom
			.map(cls -> Response.ok(mapper.map(cls, ClassroomDTO.class)).build())
			.orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity(
					new MessageResponse(
						"Classroom not found",
						CodeStatus.NOT_FOUND))
				.build());
	}
}
