package com.pshs.ams.app.fingerprints.controllers;

import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.app.fingerprints.services.FingerprintService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@ApplicationScoped
@Path("/api")
@Log4j2
public class FingerprintController {

	@Inject
	FingerprintService fingerprintService;

	@GET
	@Path("/fingerprint/{id}")
	public Response enrollFingerprint(@PathParam("id") Integer fingerprintId, @QueryParam("mode") String mode) {
		log.debug("Enrolling fingerprint with ID: {}", fingerprintId);

		Optional<MessageResponse> messageDTO = fingerprintService.enrollFingerprint(fingerprintId);
		if (messageDTO.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.ok(messageDTO.get()).build();
	}
}
