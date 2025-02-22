package com.pshs.ams.app.student_schedules.controllers;

import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.app.student_schedules.models.dto.StudentScheduleDTO;
import com.pshs.ams.app.student_schedules.models.entities.StudentSchedule;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.student_schedules.services.StudentSchedulesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/student-schedules")
public class StudentSchedulesController {

	@Inject
	StudentSchedulesService studentSchedulesService;

	private final ModelMapper mapper = new ModelMapper();

	@Path("/all")
	@GET
	public Response listAll(@BeanParam PageRequest page, @BeanParam SortRequest sort) {
		return Response.ok(
			this.studentSchedulesService.listAll(page.toPage(), sort.toSort()).stream().map(
				ss -> this.mapper.map(ss, StudentScheduleDTO.class)
			).toList()
		).build();
	}

	@Path("/create")
	@POST
	public Response create(StudentScheduleDTO studentScheduleDTO) {
		if (studentScheduleDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Student Schedule is invalid.",
				CodeStatus.BAD_REQUEST
			)).build();
		}

		Optional<StudentSchedule> studentScheduleOptional = this.studentSchedulesService.create(
			this.mapper.map(
				studentScheduleDTO, StudentSchedule.class
			)
		);

		if (studentScheduleOptional.isPresent()) {
			return Response.ok(studentScheduleOptional.get()).build();
		}

		return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
			"Please fill up all required fields.",
			CodeStatus.BAD_REQUEST
		)).build();
	}

	@Path("/{id}")
	@PUT
	public Response update(StudentScheduleDTO updatedStudentSchedule, @PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Invalid ID",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		if (updatedStudentSchedule == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Updated Student Schedule is not provided.",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		Optional<StudentSchedule> studentScheduleOptional = this.studentSchedulesService.update(
			this.mapper.map(
				updatedStudentSchedule,
				StudentSchedule.class
			), id
		);

		if (studentScheduleOptional.isPresent()) {
			return Response.ok(this.mapper.map(
				studentScheduleOptional.get(),
				StudentScheduleDTO.class
			)).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
			"Student Schedule with ID " + id + " was not found.",
			CodeStatus.NOT_FOUND
		)).build();
	}

	@GET
	@Path("/{id}")
	public Response get(@PathParam("id") Integer id) {
		Optional<StudentSchedule> studentScheduleOptional = this.studentSchedulesService.get(id);

		if (studentScheduleOptional.isPresent()) {
			return Response.ok(
				this.mapper.map(
					studentScheduleOptional.get(),
					StudentScheduleDTO.class
				)
			).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
			"Student Schedule with ID " + id + " was not found.",
			CodeStatus.NOT_FOUND
		)).build();
	}

	@Path("/{id}")
	@DELETE
	public Response delete(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Invalid ID",
				CodeStatus.BAD_REQUEST
			)).build();
		}

		Optional<StudentSchedule> studentScheduleOptional = this.studentSchedulesService.delete(id);

		if (studentScheduleOptional.isPresent()) {
			return Response.ok(
				this.mapper.map(
					studentScheduleOptional.get(),
					StudentScheduleDTO.class
				)
			).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
			"Student Schedule not found.",
			CodeStatus.NOT_FOUND
		)).build();
	}
}
