package com.pshs.ams.app.classrooms.controllers;

import com.pshs.ams.app.classrooms.exceptions.ClassroomExistsException;
import com.pshs.ams.app.classrooms.models.dto.ClassroomDTO;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.classrooms.services.ClassroomService;
import com.pshs.ams.app.students.models.dto.StudentDTO;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.global.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.hibernate.collection.spi.PersistentCollection;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/classrooms")
public class ClassroomController {

	@Inject
	ClassroomService classroomService;

	@Inject
	Logger log;

	private final ModelMapper mapper = new ModelMapper();

	public ClassroomController() {
		mapper.getConfiguration().setPropertyCondition(context ->
			!(context.getSource() instanceof PersistentCollection));
	}

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
	public Response updateClassroom(ClassroomDTO classroomInput) {
		if (classroomInput == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom cannot be null",
						CodeStatus.BAD_REQUEST))
				.build();
		}

		Optional<Classroom> classroom = this.classroomService.updateClass(
			mapper.map(classroomInput, Classroom.class)
		);

		if (classroom.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).entity(
					new MessageResponse(
						"Classroom not found",
						CodeStatus.NOT_FOUND))
				.build();
		}

		ClassroomDTO classroomDTO = mapper.map(classroom.get(), ClassroomDTO.class);

		return Response.ok(
				classroomDTO
			)
			.build();
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

		try {
			Optional<Classroom> createdClassroom = classroomService.createClass(classroom);
			if (createdClassroom.isEmpty()) {
				return Response.status(Response.Status.CONFLICT).entity(
						new MessageResponse(
							"Classroom already exists",
							CodeStatus.CONFLICT))
					.build();
			}

			return Response.ok(
					mapper.map(createdClassroom.get(), ClassroomDTO.class)
				)
				.build();
		} catch (IllegalArgumentException | ClassroomExistsException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						e.getMessage(),
						CodeStatus.BAD_REQUEST))
				.build();
		}
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

		try {
			classroomService.deleteClassroom(id);
			return Response.ok(
				new MessageResponse(
					"Classroom deleted",
					CodeStatus.OK)
			).build();
		} catch (IllegalArgumentException | ClassroomExistsException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						e.getMessage(),
						CodeStatus.BAD_REQUEST))
				.build();
		}
	}

	@GET
	@Path("/search/name")
	public Response searchClassroomByName(@QueryParam("name") String name, @BeanParam PageRequest pageRequest,
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

	@PUT
	@Path("/{id}/assign-students")
	public Response assignStudentsToClassroom(@PathParam("id") Long classroomId, List<StudentDTO> students) {
		if (classroomId == null || classroomId <= 0 || students == null || students.isEmpty()) {
			log.debug("assignStudentsToClassroom() - Classroom id cannot be less than or equal to zero or students cannot be null or empty");
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Classroom id cannot be less than or equal to zero or students cannot be null or empty",
						CodeStatus.BAD_REQUEST))
				.build();
		}

		try {
			log.debug("assignStudentsToClassroom() - Assigning students to classroom");
			List<Student> assignedStudents = classroomService.assignStudentsToClassroom(
				classroomId,
				students.stream().map(st -> mapper.map(st, Student.class)).toList()
			);
			log.debug("assignStudentsToClassroom() - Students assigned to classroom");
			return Response.ok(
					assignedStudents.stream().map(st -> mapper.map(st, StudentDTO.class)).toList()
				)
				.build();
		} catch (ClassroomExistsException | IllegalArgumentException e) {
			log.error("assignStudentsToClassroom() - Error assigning students to classroom", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						e.getMessage(),
						CodeStatus.BAD_REQUEST))
				.build();
		}
	}
}
