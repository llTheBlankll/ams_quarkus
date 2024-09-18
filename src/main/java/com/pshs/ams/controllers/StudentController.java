package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.custom.PageRequest;
import com.pshs.ams.models.dto.custom.SortRequest;
import com.pshs.ams.models.dto.student.StudentDTO;
import com.pshs.ams.services.StudentService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.modelmapper.ModelMapper;

import java.util.List;

@Path("/api/v1/student")
@ApplicationScoped
@Tag(name = "Student API")
public class StudentController {

	@Inject
	StudentService studentService;
	private final ModelMapper modelMapper = new ModelMapper();

	/**
	 * Retrieves a list of all students with optional pagination and sorting.
	 *
	 * @param sortRequest  an object containing sorting parameters (sortBy, sortDirection)
	 * @param pageRequest  an object containing pagination parameters (page, size)
	 * @return          a list of StudentDTO objects
	 */
	@GET
	@Path("/all")
	@Operation(summary = "Get All Students", description = "Get all students.")
	@Parameters(
		value = {
			@Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.INTEGER, format = "int32")),
			@Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.INTEGER, format = "int32")),
			@Parameter(name = "sortBy", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.STRING)),
			@Parameter(name = "sortDirection", in = ParameterIn.QUERY, schema = @Schema(type = SchemaType.STRING))
		}
	)
	public List<StudentDTO> getAllStudent(
		@BeanParam SortRequest sortRequest,
		@BeanParam PageRequest pageRequest
	) {
		return this.studentService.getAllStudents(
			Sort.by(sortRequest.sortBy, sortRequest.sortDirection),
			Page.of(pageRequest.page, pageRequest.size)
		).stream().map(student -> this.modelMapper.map(student, StudentDTO.class)).toList();
	}


}