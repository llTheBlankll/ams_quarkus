package com.pshs.ams.app.reports.controllers;

import com.pshs.ams.app.classrooms.exceptions.ClassroomNotFoundException;
import com.pshs.ams.app.reports.services.ReportService;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.enums.CodeStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/api/v1/reports")
public class ReportController {

	@Inject
	ReportService reportService;

	@Inject
	Logger log;

	@Path("/generate/sf2")
	public Response generateSF2Report(@QueryParam("classroomId") Integer id) {
		try {
			reportService.generateSF2Report(id);
			return Response.ok(new MessageResponse(
				"SF2 report generated successfully",
				CodeStatus.OK
			)).build();
		} catch (ClassroomNotFoundException e) {
			log.error("Classroom not found with ID: " + id);
			return Response.status(Response.Status.NOT_FOUND).entity(new MessageResponse(
				e.getMessage(),
				CodeStatus.FAILED
			)).build();
		} catch (IllegalArgumentException e) {
			log.error("Error generating SF2 report", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					e.getMessage(),
					CodeStatus.FAILED
				)
			).build();
		}
	}
}
