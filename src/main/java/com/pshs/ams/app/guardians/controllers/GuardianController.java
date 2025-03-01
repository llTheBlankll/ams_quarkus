package com.pshs.ams.app.guardians.controllers;

import com.pshs.ams.app.guardians.exceptions.GuardianExistsException;
import com.pshs.ams.app.guardians.exceptions.GuardianNotFoundException;
import com.pshs.ams.app.guardians.models.dto.GuardianDTO;
import com.pshs.ams.app.guardians.models.dto.GuardianInput;
import com.pshs.ams.app.guardians.models.entities.Guardian;
import com.pshs.ams.app.guardians.services.GuardianService;
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
			guardianService.listAll(Sort.by(sortRequest.sortBy, sortRequest.sortDirection), Page.of(pageRequest.page, pageRequest.size)).stream().map(guardian -> mapper.map(guardian, GuardianDTO.class)).toList()
		).build();
	}

	@GET
	@Path("/{id}")
	public Response getGuardianById(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse("Invalid id", CodeStatus.BAD_REQUEST)).build();
		}
		Optional<Guardian> guardian = guardianService.get(id);
		if (guardian.isPresent()) {
			return Response.ok(
				mapper.map(guardian.get(), GuardianDTO.class)
			).build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse("Guardian not found", CodeStatus.NOT_FOUND)).build();
	}

	@POST
	@Path("/create")
	public Response createGuardian(GuardianInput guardianInput) {
		if (guardianInput == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Guardian was not provided",
					CodeStatus.BAD_INPUT
				)
			).build();
		}

		Guardian guardian = mapper.map(guardianInput, Guardian.class);
		try {
			Optional<Guardian> guardianOptional = guardianService.create(guardian);
			if (guardianOptional.isEmpty()) {
				return Response.status(Response.Status.CONFLICT).entity(new MessageResponse(
					"Guardian already exists",
					CodeStatus.CONFLICT
				)).build();
			}

			return Response.ok(
				mapper.map(guardianOptional.get(), GuardianDTO.class)
			).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Invalid input. Please check if the input is correct.",
					CodeStatus.BAD_REQUEST
				)
			).build();
		} catch (GuardianExistsException e) {
			return Response.status(Response.Status.CONFLICT).entity(new MessageResponse(
				"Guardian already exists",
				CodeStatus.CONFLICT
			)).build();
		}
	}

	@PUT
	@Path("/update")
	public Response updateGuardian(GuardianDTO guardianDTO, @QueryParam("id") Integer id) {
		if (guardianDTO == null || id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Guardian was not provided",
					CodeStatus.BAD_INPUT
				)
			).build();
		}
		Guardian guardian = mapper.map(guardianDTO, Guardian.class);
		try {
			Optional<Guardian> updatedGuardian = guardianService.update(guardian, id);

			if (updatedGuardian.isEmpty()) {
				return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
					"Guardian not found",
					CodeStatus.NOT_FOUND
				)).build();
			}

			return Response.ok(
				mapper.map(updatedGuardian.get(), GuardianDTO.class)
			).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Invalid input. Please check if the input is correct.",
					CodeStatus.BAD_REQUEST
				)).build();
		} catch (GuardianNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
				"Guardian not found",
				CodeStatus.NOT_FOUND
			)).build();
		}
	}

	@DELETE
	@Path("/{id}")
	public Response deleteGuardian(@PathParam("id") Integer id) {
		if (id <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Guardian id cannot be less than or equal to zero or was not provided.",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		try {
			guardianService.delete(id);
			return Response.ok(new MessageResponse("Guardian deleted", CodeStatus.OK)).build();
		} catch (GuardianNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
				"Guardian not found",
				CodeStatus.NOT_FOUND
			)).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Invalid input. Please check if the input is correct.",
				CodeStatus.BAD_REQUEST
			)).build();
		}
	}
}
