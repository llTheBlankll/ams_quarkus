package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.custom.JWTInformationDTO;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.user.LoginRequest;
import com.pshs.ams.models.dto.user.UserDTO;
import com.pshs.ams.models.entities.User;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.UserService;
import io.quarkus.security.Authenticated;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ApplicationScoped
@Path("/api/v1/users")
public class UserController {

	@Inject
	Logger logger;

	@Inject
	UserService userService;

	private final ModelMapper mapper = new ModelMapper();

	@GET
	@Path("/me")
	@Authenticated
	public Response currentUser(@Context SecurityContext ctx) {
		logger.info("Hello " + ctx.getUserPrincipal().toString());
		DefaultJWTCallerPrincipal principal = (DefaultJWTCallerPrincipal) ctx.getUserPrincipal();

		Optional<User> user = User.find("username", principal.getName()).firstResultOptional();
		if (user.isPresent()) {
			return Response.ok(
				mapper.map(user.get(), UserDTO.class)
			).build();
		}

		return Response.status(Response.Status.UNAUTHORIZED).entity(
			new MessageDTO(
				"User not found",
				CodeStatus.FAILED
			)
		).build();
	}

	@GET
	@Path("/validate")
	@PermitAll
	public Response validateJWT(@Context SecurityContext context) {
		DefaultJWTCallerPrincipal principal = (DefaultJWTCallerPrincipal) context.getUserPrincipal();

		if (principal.getExpirationTime() < System.currentTimeMillis()) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(
				new MessageDTO(
					"Token expired",
					CodeStatus.FAILED
				)
			).build();
		}

		if (principal.getClaimNames().isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(
				new MessageDTO(
					"Invalid token",
					CodeStatus.FAILED
				)
			).build();
		}

		// * Check if user exists
		Optional<User> user = User.find("username", principal.getName()).firstResultOptional();
		if (user.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(
				new MessageDTO(
					"User not found",
					CodeStatus.FAILED
				)
			).build();
		}

		return Response.ok(
			new MessageDTO(
				"Token valid",
				CodeStatus.OK
			)
		).build();
	}

	@POST
	@Path("/login")
	public Response login(LoginRequest loginRequest) {
		logger.info("Login request: " + loginRequest.toString());
		if (loginRequest.getUsername().isEmpty() || loginRequest.getPassword().isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(
				new MessageDTO(
					"Invalid credentials",
					CodeStatus.FAILED
				)
			).build();
		}

		Optional<JWTInformationDTO> response = Optional.ofNullable(userService.login(loginRequest));
		if (response.isEmpty()) {
			return Response.status(Response.Status.UNAUTHORIZED).entity(
				new MessageDTO(
					"Invalid credentials",
					CodeStatus.FAILED
				)
			).build();
		}

		return Response.ok(
			response.get()
		).build();
	}
}