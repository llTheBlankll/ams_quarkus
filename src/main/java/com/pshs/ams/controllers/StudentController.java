package com.pshs.ams.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.modelmapper.ModelMapper;

import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.PageRequest;
import com.pshs.ams.models.dto.custom.SortRequest;
import com.pshs.ams.models.dto.strand.MostPopularStrandDTO;
import com.pshs.ams.models.dto.student.StudentDTO;
import com.pshs.ams.models.entities.Student;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.StudentService;

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
			@BeanParam PageRequest pageRequest) {
		return this.studentService.getAllStudents(
				Sort.by(sortRequest.sortBy, sortRequest.sortDirection),
				Page.of(pageRequest.page, pageRequest.size)).stream()
				.map(student -> this.modelMapper.map(student, StudentDTO.class)).toList();
	}

	@POST
	@Path("/create")
	public Response createStudent(StudentDTO studentDTO) {
		if (studentDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(
					"Student is not provided",
					CodeStatus.NULL)).build();
		}

		Student student = this.modelMapper.map(studentDTO, Student.class);
		return switch (studentService.createStudent(student)) {
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(
					"Invalid student",
					CodeStatus.BAD_REQUEST)).build();
			case EXISTS -> Response.ok(new MessageDTO(
					"Student already exists",
					CodeStatus.EXISTS)).build();
			case OK -> Response.status(Response.Status.CREATED).entity(
					new MessageDTO(
							"Student created",
							CodeStatus.OK))
					.build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO(
					"Internal server error",
					CodeStatus.FAILED)).build();
		};
	}

	@DELETE
	@Path("/{id}")
	public Response deleteStudent(@PathParam("id") Long id) {
		return switch (studentService.deleteStudent(id)) {
			case OK -> Response.ok(new MessageDTO(
					"Student deleted",
					CodeStatus.OK)).build();
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(
					"Invalid id",
					CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND -> Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO(
					"Student not found",
					CodeStatus.NOT_FOUND)).build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO(
					"Internal server error",
					CodeStatus.FAILED)).build();
		};
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
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO(
					"Student not found",
					CodeStatus.NOT_FOUND)).build();
		}
	}

	@GET
	@Path("/search/name/{name}")
	public Response searchStudentByName(@PathParam("name") String name,
			@BeanParam SortRequest sortRequest,
			@BeanParam PageRequest pageRequest) {
		return Response.ok(
				studentService.searchStudentByName(name,
						Sort.by(sortRequest.sortBy, sortRequest.sortDirection),
						Page.of(pageRequest.page, pageRequest.size))
						.stream()
						.map(student -> modelMapper.map(student, StudentDTO.class)).toList())
				.build();
	}

	@PUT
	@Path("/{id}/assign-classroom/{classroomId}")
	public Response assignClassroomToStudent(@PathParam("id") Long id, @PathParam("classroomId") Long classroomId) {
		return switch (studentService.assignClassroomToStudent(id, classroomId)) {
			case OK -> Response.ok(new MessageDTO(
					"Classroom assigned to student",
					CodeStatus.OK)).build();
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO(
					"Invalid id or classroom id",
					CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND -> Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO(
					"Student or classroom not found",
					CodeStatus.NOT_FOUND)).build();
			default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO(
					"Internal server error",
					CodeStatus.FAILED)).build();
		};
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
            @QueryParam("endDate") String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        LineChartDTO chartData = studentService.getStrandDistribution(startDate, endDate);
        return Response.ok(chartData).build();
    }
}
