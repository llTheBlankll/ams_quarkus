package com.pshs.ams.app.students.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.pshs.ams.app.students.exceptions.StudentExistsException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import com.pshs.ams.global.models.custom.LineChart;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.app.strands.models.dto.MostPopularStrandDTO;
import com.pshs.ams.app.students.models.dto.StudentDTO;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.students.services.StudentService;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/students")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentController {

	@Inject
	StudentService studentService;
	private final ModelMapper modelMapper = new ModelMapper();
	@Inject
	Logger logger;

	/**
	 * Retrieves a list of all students with optional pagination and sorting.
	 *
	 * @param sortRequest an object containing sorting parameters (sortBy,
	 *                    sortDirection)
	 * @param pageRequest an object containing pagination parameters (page, size)
	 * @return a list of StudentDTO objects
	 */
	@GET
	@Path("/all")
	@Operation(summary = "Get All Students", description = "Get all students.")
	// @Parameters(value = {
	// 		@Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.INTEGER, format = "int32")),
	// 		@Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.INTEGER, format = "int32")),
	// 		@Parameter(name = "sortBy", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.STRING)),
	// 		@Parameter(name = "sortDirection", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.STRING))
	// })
	public List<StudentDTO> getAllStudent(
		@BeanParam SortRequest sortRequest,
		@BeanParam PageRequest pageRequest
	) {
		return this.studentService.getAllStudents(
				Sort.by(sortRequest.sortBy, sortRequest.sortDirection),
				Page.of(pageRequest.page, pageRequest.size)
			).stream()
			.map(student -> this.modelMapper.map(student, StudentDTO.class)).toList();
	}

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createStudent(StudentDTO studentDTO) {
		if (studentDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Student is not provided",
				CodeStatus.NULL
			)).build();
		}
		Student student = this.modelMapper.map(studentDTO, Student.class);
		try {
			Optional<Student> studentOptional = studentService.createStudent(student);
			if (studentOptional.isEmpty()) {
				return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
					"Student already exists",
					CodeStatus.BAD_REQUEST
				)).build();
			}

			return Response.ok(
				modelMapper.map(studentOptional.get(), StudentDTO.class)
			).build();
		} catch (StudentExistsException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Student already exists",
				CodeStatus.EXISTS
			)).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Invalid data received.",
				CodeStatus.BAD_REQUEST
			)).build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response deleteStudent(@PathParam("id") Long id) {
		try {
			studentService.deleteStudent(id);
			return Response.ok(new MessageResponse(
				"Student deleted",
				CodeStatus.OK
			)).build();
		} catch (StudentExistsException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Student not found",
				CodeStatus.NOT_FOUND
			)).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Invalid data received.",
				CodeStatus.BAD_REQUEST
			)).build();
		}
	}

	@GET
	@Path("/count")
	public Response getTotalCount() {
		long count = studentService.getTotalStudents();
		return Response.ok(count).build();
	}

	@GET
	@Path("/count/classroom/{id}")
	public Response getStudentTotalCountInClassroom(@PathParam("id") Long id) {
		long count = studentService.getTotalStudents(id);
		return Response.ok(count).build();
	}

	@GET
	@Path("/{id}")
	public Response getStudentById(@PathParam("id") Long id) {
		Optional<Student> student = studentService.getStudent(id);
		if (student.isPresent()) {
			return Response.ok(
				modelMapper.map(student.get(), StudentDTO.class)).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
				"Student not found",
				CodeStatus.NOT_FOUND
			)).build();
		}
	}

	@GET
	@Path("/search/name/{name}")
	public Response searchStudentByName(
		@PathParam("name") String name,
		@BeanParam SortRequest sortRequest,
		@BeanParam PageRequest pageRequest
	) {
		return Response.ok(
				studentService.searchStudentByName(
						name,
						Sort.by(sortRequest.sortBy, sortRequest.sortDirection),
						Page.of(pageRequest.page, pageRequest.size)
					)
					.stream()
					.map(student -> modelMapper.map(student, StudentDTO.class)).toList())
			.build();
	}

	@PUT
	@Path("/{id}/assign-classroom")
	public Response assignClassroomToStudent(@PathParam("id") Long id, @QueryParam("classroomId") Long classroomId) {
		try {
			return switch (studentService.assignStudentToClassroom(id, classroomId)) {
				case OK -> Response.ok(new MessageResponse(
					"Classroom assigned to student",
					CodeStatus.OK
				)).build();
				case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
					"Invalid id or classroom id",
					CodeStatus.BAD_REQUEST
				)).build();
				case NOT_FOUND -> Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
					"Student or classroom not found",
					CodeStatus.NOT_FOUND
				)).build();
				default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageResponse(
					"Internal server error",
					CodeStatus.FAILED
				)).build();
			};
		} catch (StudentExistsException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				e.getMessage(),
				CodeStatus.NOT_FOUND
			)).build();
		}
	}

	@GET
	@Path("/count/strand/{strandId}")
	@Operation(summary = "Get student count by strand")
	public Response getStudentCountByStrand(@PathParam("strandId") Long strandId) {
		long count = studentService.getStudentCountByStrand(strandId);
		return Response.ok(count).build();
	}

	@GET
	@Path("/count/grade-level/{gradeLevelId}")
	@Operation(summary = "Get student count by grade level")
	public Response getStudentCountByGradeLevel(@PathParam("gradeLevelId") Long gradeLevelId) {
		long count = studentService.getStudentCountByGradeLevel(gradeLevelId);
		return Response.ok(count).build();
	}

	@GET
	@Path("/most-popular-strand")
	@Operation(summary = "Get most popular strand")
	public Response getMostPopularStrand() {
		Optional<MostPopularStrandDTO> result = studentService.getMostPopularStrand();
		if (result.isPresent()) {
			return Response.ok(result.get()).build();
		}

		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@GET
	@Path("/average-per-strand")
	@Operation(summary = "Get average students per strand")
	public Response getAverageStudentsPerStrand() {
		double average = studentService.getAverageStudentsPerStrand();
		return Response.ok(average).build();
	}

	@GET
	@Path("/strand-distribution")
	@Operation(summary = "Get strand distribution")
	public Response getStrandDistribution(
		@QueryParam("startDate") String startDateStr,
		@QueryParam("endDate") String endDateStr
	) {
		LocalDate startDate = LocalDate.parse(startDateStr);
		LocalDate endDate = LocalDate.parse(endDateStr);
		LineChart chartData = studentService.getStrandDistribution(startDate, endDate);
		return Response.ok(chartData).build();
	}
}
