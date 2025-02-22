package com.pshs.ams.app.strands.controllers;

import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import com.pshs.ams.app.strands.models.dto.StrandDTO;
import com.pshs.ams.app.strands.models.entities.Strand;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.strands.services.StrandService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/strands")
public class StrandController {

	@Inject
	StrandService strandService;
	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllStrands(@BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		return Response.ok(strandService.getAllStrand(
			Sort.by(sortRequest.sortBy, sortRequest.sortDirection),
			Page.of(pageRequest.page, pageRequest.size)
		).stream().map(strand -> mapper.map(strand, StrandDTO.class)).toList()).build();
	}

	@POST
	@Path("/create")
	public Response createStrand(StrandDTO strandDTO) {
		if (strandDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid strand content.", CodeStatus.BAD_REQUEST)).build();
		}
		Strand strand = mapper.map(strandDTO, Strand.class);
		Optional<Strand> strandOptional = strandService.createStrand(strand);

		if (strandOptional.isPresent()) {
			return Response.ok(mapper.map(strandOptional.get(), StrandDTO.class)).build();
		}

		return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
			"Strand was not created.",
			CodeStatus.BAD_REQUEST
		)).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteStrand(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		return switch (strandService.deleteStrand(id)) {
			case OK -> Response.ok(new MessageResponse("Strand deleted", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Strand not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageResponse("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}

	@GET
	@Path("/{id}")
	public Response getStrandById(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}
		Optional<Strand> strand = strandService.getStrand(id);
		if (strand.isPresent()) {
			return Response.ok(mapper.map(strand.get(), StrandDTO.class)).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Strand not found", CodeStatus.NOT_FOUND)).build();
	}

	@PUT
	@Path("/{id}")
	public Response updateStrand(StrandDTO strandDTO, @PathParam("id") Integer id) {
		if (strandDTO == null || id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		Strand strand = mapper.map(strandDTO, Strand.class);
		CodeStatus status = strandService.updateStrand(strand, id);
		return switch (status) {
			case OK -> Response.ok(new MessageResponse("Strand updated", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Strand not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageResponse("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}
}
