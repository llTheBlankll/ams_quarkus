package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.PageRequest;
import com.pshs.ams.models.dto.custom.SortRequest;
import com.pshs.ams.models.dto.guardian.GuardianDTO;
import com.pshs.ams.models.entities.Guardian;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.GuardianService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/guardians")
public class GuardianController {

	@Inject
	GuardianService guardianService;
	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/all")
	public Response getAllGuardians(@BeanParam PageRequest pageRequest, @BeanParam SortRequest sortRequest) {
		return Response.ok(
			guardianService.getAllGuardian(Sort.by(sortRequest.sortBy, sortRequest.sortDirection), Page.of(pageRequest.page, pageRequest.size)).stream().map(guardian -> mapper.map(guardian, GuardianDTO.class)).toList()
		).build();
	}

	@GET
	@Path("/{id}")
	public Response getGuardianById(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageDTO("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}
		Optional<Guardian> guardian = guardianService.getGuardianById(id);
		if (guardian.isPresent()) {
			return Response.ok(
				mapper.map(guardian.get(), GuardianDTO.class)
			).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageDTO("Guardian not found", CodeStatus.NOT_FOUND)).build();
	}

	@POST
	@Path("/create")
	public Response createGuardian(GuardianDTO guardianDTO) {
		if (guardianDTO == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Guardian was not provided",
					CodeStatus.NULL
				)
			).build();
		}

		Guardian guardian = mapper.map(guardianDTO, Guardian.class);
		CodeStatus status = guardianService.createGuardian(guardian);
		return switch (status) {
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Guardian was not created",
					CodeStatus.BAD_REQUEST
				)
			).build();
			case EXISTS -> Response.ok(
				new MessageDTO(
					"Guardian already exists",
					CodeStatus.EXISTS
				)
			).build();
			case OK -> Response.ok(
				new MessageDTO(
					"Guardian was created",
					CodeStatus.OK
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

	@PUT
	@Path("/update")
	public Response updateGuardian(GuardianDTO guardianDTO, @QueryParam("id") Integer id) {
		if (guardianDTO == null || id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Guardian was not provided",
					CodeStatus.NULL
				)
			).build();
		}
		Guardian guardian = mapper.map(guardianDTO, Guardian.class);
		CodeStatus status = guardianService.updateGuardian(guardian, id);
		return switch (status) {
			case BAD_REQUEST -> Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Guardian was not updated",
					CodeStatus.BAD_REQUEST
				)
			).build();
			case NOT_FOUND -> Response.ok(
				new MessageDTO(
					"Guardian not found",
					CodeStatus.NOT_FOUND
				)
			).build();
			case OK -> Response.ok(
				new MessageDTO(
					"Guardian was updated",
					CodeStatus.OK
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
	public Response deleteGuardian(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageDTO(
					"Guardian id cannot be less than or equal to zero or was not provided.",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		return switch (guardianService.deleteGuardian(id)) {
			case OK -> Response.ok(
				new MessageDTO(
					"Guardian was deleted",
					CodeStatus.OK
				)
			).build();
			case NOT_FOUND -> Response.ok(
				new MessageDTO(
					"Guardian not found",
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
}