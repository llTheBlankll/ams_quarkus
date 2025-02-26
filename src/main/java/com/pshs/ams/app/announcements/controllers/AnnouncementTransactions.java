package com.pshs.ams.app.announcements.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;

import com.pshs.ams.app.announcements.models.dto.AnnouncementDTO;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.app.announcements.models.entities.Announcement;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.announcements.services.AnnouncementService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/api/v1/announcements")
public class AnnouncementTransactions {

	Logger logger = LogManager.getLogger(this.getClass());

	@Inject
	AnnouncementService announcementService;

	private final ModelMapper mapper = new ModelMapper();

	@Path("/create")
	@POST
	public Response createAnnouncement(AnnouncementDTO announcementDTO) {
		// To prevent empty message announcements.
		if (announcementDTO == null) {
			logger.debug("Announcement is not provided, please try again.");
			return Response.status(Response.Status.BAD_REQUEST).entity(new MessageResponse(
				"Announcement Body is not provided. Please try again.",
				CodeStatus.BAD_REQUEST
			)).build();
		}

		logger.debug("Creation of Announcement with title {}", announcementDTO.getTitle());
		CodeStatus status = announcementService.createAnnouncement(
			mapper.map(announcementDTO, Announcement.class)
		);
		switch (status) {
			case OK -> {
				return Response.ok(new MessageResponse(
					"Announcement with title " + announcementDTO.getTitle() + " was successfully created",
					CodeStatus.OK
				)).build();
			}
			case CONFLICT -> {
				return Response.ok(new MessageResponse(
					"Announcement with ID " + announcementDTO.getId() + " already exists.",
					CodeStatus.CONFLICT
				)).build();
			}
			default -> {
				logger.error("ERROR: CANNOT CREATE ANNOUNCEMENT");
				return Response.serverError().entity(new MessageResponse(
					"Server Error Occurred.",
					CodeStatus.FAILED
				)).build();
			}
		}
	}

	@Path("/{id}")
	@PUT
	public Response updateAnnouncement(@PathParam("id") Integer announcementId, AnnouncementDTO announcementDTO) {
		if (announcementId == null) {
			logger.debug("Update not finished, invalid ID received.");
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Please fill up the announcement id to know which to update.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		// Check if announcement exists and update it
		CodeStatus status = announcementService.updateAnnouncement(
			mapper.map(announcementDTO, Announcement.class),
			announcementId
		);

		switch (status) {
			case OK -> {
				return Response.ok(new MessageResponse(
					"Announcement with ID " + announcementId + " was successfully updated",
					CodeStatus.OK
				)).build();
			}
			case NOT_FOUND -> {
				return Response.status(Response.Status.NOT_FOUND).entity(
						new MessageResponse(
							"Announcement with ID " + announcementId + " was not found",
							CodeStatus.NOT_FOUND
						))
					.build();
			}
			default -> {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
						new MessageResponse(
							"An error occurred while updating the announcement",
							CodeStatus.FAILED
						))
					.build();
			}
		}
	}

	@Path("/{id}")
	@DELETE
	public Response deleteAnnouncement(@PathParam("id") Integer announcementId) {
		if (announcementId == null) {
			logger.debug("Delete not finished, invalid ID received.");
			return Response.status(Response.Status.BAD_REQUEST).entity(
					new MessageResponse(
						"Please fill up the announcement id to know which to delete.",
						CodeStatus.BAD_REQUEST
					))
				.build();
		}

		if (announcementService.isExist(announcementId)) {
			announcementService.deleteAnnouncement(announcementId);
			return Response
				.ok(new MessageResponse("Announcement with ID " + announcementId + " was successfully deleted", CodeStatus.OK))
				.build();
		}

		return Response.status(Response.Status.NOT_FOUND).entity(
				new MessageResponse(
					"Announcement with ID " + announcementId + " was not found",
					CodeStatus.NOT_FOUND
				))
			.build();
	}
}
