package com.pshs.ams.app.announcements.controllers;

import java.util.List;
import java.util.Optional;

import com.pshs.ams.global.models.custom.PageRequest;
import com.pshs.ams.global.models.custom.SortRequest;
import jakarta.ws.rs.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;

import com.pshs.ams.app.announcements.models.dto.AnnouncementDTO;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.app.announcements.models.entities.Announcement;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.announcements.services.AnnouncementService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/api/v1/announcements")
public class AnnouncementRetrieval {

	Logger logger = LogManager.getLogger(this.getClass());

	@Inject
	AnnouncementService announcementService;

	private final ModelMapper mapper = new ModelMapper();

	@Path("/{id}")
	@GET
	public Response getAnnouncement(@PathParam("id") Integer id) {
		logger.debug("Get Announcement: {}", id);
		Optional<Announcement> announcementOptional = announcementService.getAnnouncement(id);

		if (announcementOptional.isEmpty()) {
			return Response.status(404).entity(
					new MessageResponse(
						"Announcement not found",
						CodeStatus.NOT_FOUND
					))
				.build();
		}

		return Response.ok(
			mapper.map(announcementOptional.get(), AnnouncementDTO.class)).build();
	}

	@Path("/search")
	@GET
	public Response searchAnnouncement(@QueryParam("q") String query, @BeanParam PageRequest page, @BeanParam SortRequest sort) {
		// To prevent unnecessary request of empty query.
		if (query.isEmpty()) {
			logger.debug("Search Announcement Query empty");
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Search Query cannot be empty. Please fill in 'q' parameter.",
					CodeStatus.BAD_REQUEST
				)
			).build();
		}

		logger.debug("Search Announcement Query: {}", query);
		List<AnnouncementDTO> announcementDTOList = announcementService.searchAnnouncement(query, sort.toSort(), page.toPage())
			.stream()
			.map(a ->
				mapper.map(a, AnnouncementDTO.class)
			).toList();
		return Response.ok(
			announcementDTOList
		).build();
	}

	@Path("/all")
	@GET
	public Response getAllAnnouncements(@BeanParam PageRequest page, @BeanParam SortRequest sort) {
		return Response.ok(
			announcementService.getAllAnnouncements(sort.toSort(), page.toPage())
				.stream()
				.map(
					a -> mapper.map(a, AnnouncementDTO.class)
				).toList()
		).build();
	}
}
