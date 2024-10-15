package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.PageRequest;
import com.pshs.ams.models.dto.custom.SortRequest;
import com.pshs.ams.models.dto.teacher.TeacherDTO;
import com.pshs.ams.models.entities.Teacher;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.TeacherService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/teachers")
public class TeacherController {

	@Inject
	TeacherService teacherService;
	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllTeacher(@BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		return Response.ok(teacherService.getAllTeacher(Sort.by(sortRequest.sortBy, sortRequest.sortDirection), Page.of(pageRequest.page, pageRequest.size))
			.stream().map(teacher -> mapper.map(teacher, TeacherDTO.class)).toList()
		).build();
	}

	@GET
	@Path("/{id}")
	public Response getTeacherById(@PathParam("id") Long id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		Optional<Teacher> teacher = teacherService.getTeacher(id);
		return teacher.map(tch -> Response.ok(mapper.map(tch, TeacherDTO.class)).build()).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Teacher not found", CodeStatus.NOT_FOUND)).build());
	}

	@POST
	@Path("/create")
	public Response createTeacher(TeacherDTO teacherDTO) {
		if (teacherDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Teacher cannot be null", CodeStatus.BAD_REQUEST)).build();
		}

		Teacher teacher = mapper.map(teacherDTO, Teacher.class);
		TeacherDTO teacherResponse = mapper.map(teacherService.createTeacher(teacher), TeacherDTO.class);

		if (teacherResponse == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Teacher cannot be null", CodeStatus.BAD_REQUEST)).build();
		}

		return Response.ok(teacherResponse).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteTeacher(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		return switch (teacherService.deleteTeacher(id)) {
			case OK -> Response.ok(new MessageDTO("Teacher deleted", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Teacher not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}

	@GET
	@Path("/search/name/{name}")
	public Response getTeacherByName(@PathParam("name") String name, @BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		if (name.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Name cannot be empty", CodeStatus.BAD_REQUEST)).build();
		}

		List<Teacher> teacherList = teacherService.searchTeacherByName(name, Page.of(pageRequest.page, pageRequest.size), Sort.by(sortRequest.sortBy, sortRequest.sortDirection));
		return Response.ok(teacherList.stream().map(tch -> mapper.map(tch, TeacherDTO.class)).toList()).build();
	}

	@PUT
	@Path("/update/{id}")
	public Response updateTeacher(TeacherDTO teacherDTO, @PathParam("id") Long id) {
		if (teacherDTO == null || id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Teacher cannot be null or id cannot be less than or equal to zero", CodeStatus.BAD_REQUEST)).build();
		}

		// Check if exists
		Optional<Teacher> teacherOptional = teacherService.getTeacher(id);
		if (teacherOptional.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Teacher not found", CodeStatus.NOT_FOUND)).build();
		}

		Teacher teacher = mapper.map(teacherDTO, Teacher.class);
		return switch (teacherService.updateTeacher(teacher)) {
			case OK -> Response.ok(new MessageDTO("Teacher updated", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Teacher cannot be null", CodeStatus.BAD_REQUEST)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}
}