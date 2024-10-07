package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.PageRequest;
import com.pshs.ams.models.dto.custom.SortRequest;
import com.pshs.ams.models.dto.strand.StrandDTO;
import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.StrandService;
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
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}
		Strand strand = mapper.map(strandDTO, Strand.class);
		CodeStatus status = strandService.createStrand(strand);

		return switch (status) {
			case OK -> Response.ok(new MessageDTO("Strand created", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Strand not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}

	@DELETE
	@Path("/{id}")
	public Response deleteStrand(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		return switch (strandService.deleteStrand(id)) {
			case OK -> Response.ok(new MessageDTO("Strand deleted", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Strand not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}

	@GET
	@Path("/{id}")
	public Response getStrandById(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}
		Optional<Strand> strand = strandService.getStrand(id);
		if (strand.isPresent()) {
			return Response.ok(mapper.map(strand.get(), StrandDTO.class)).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Strand not found", CodeStatus.NOT_FOUND)).build();
	}

	@PUT
	@Path("/update")
	public Response updateStrand(StrandDTO strandDTO, @QueryParam("id") Integer id) {
		if (strandDTO == null || id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}

		Strand strand = mapper.map(strandDTO, Strand.class);
		CodeStatus status = strandService.updateStrand(strand, id);
		return switch (status) {
			case OK -> Response.ok(new MessageDTO("Strand updated", CodeStatus.OK)).build();
			case BAD_REQUEST ->
				Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
			case NOT_FOUND ->
				Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Strand not found", CodeStatus.NOT_FOUND)).build();
			default ->
				Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new MessageDTO("Internal Server Error", CodeStatus.FAILED)).build();
		};
	}
}