package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.classroom.ClassroomDTO;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.entities.Classroom;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.ClassroomService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/classrooms")
public class ClassroomController {

	@Inject
	ClassroomService classroomService;

	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllClassrooms(Page page, Sort sort) {
		return Response.ok(
			classroomService.getAllClasses(sort, page)
		).build();
	}

	@PUT
	@Path("/create")
	public Response createClassroom(ClassroomDTO classroomDTO) {
		if (classroomDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		// Convert the dto to entity
		Classroom classroom = mapper.map(classroomDTO, Classroom.class);
		CodeStatus status = classroomService.createClass(classroom);

		return switch (status) {
			case OK -> Response.ok(
				new MessageDTO(
					"Classroom created",
					CodeStatus.OK
				)
			).build();
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Classroom cannot be null",
					CodeStatus.BAD_REQUEST
				)
			).build();
			case EXISTS -> Response.ok(
				new MessageDTO(
					"Classroom already exists",
					CodeStatus.EXISTS
				)
			).build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
				new MessageDTO(
					"Internal Server Error",
					CodeStatus.FAILED
				)
			).build();
		};
	}

	@DELETE
	@Path("/{id}")
	public Response deleteClassroom(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Classroom id cannot be less than or equal to zero",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		return switch (classroomService.deleteClassroom(id)) {
			case OK -> Response.ok(
				new MessageDTO(
					"Classroom deleted",
					CodeStatus.OK
				)
			).build();
			case NOT_FOUND -> Response.status(Response.Status.NOT_FOUND).entity(
				new MessageDTO(
					"Classroom not found",
					CodeStatus.NOT_FOUND
				)
			).build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
				new MessageDTO(
					"Internal Server Error",
					CodeStatus.FAILED
				)
			).build();
		};
	}


	@GET
	@Path("/{id}")
	public Response getClassroom(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Classroom id cannot be less than or equal to zero",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}
		Optional<Classroom> classroom = classroomService.getClassroom(id);
		return classroom
			.map(cls -> Response.ok(mapper.map(cls, ClassroomDTO.class)).build())
			.orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity(
				new MessageDTO(
					"Classroom not found",
					CodeStatus.NOT_FOUND
				)
			).build());
	}
}