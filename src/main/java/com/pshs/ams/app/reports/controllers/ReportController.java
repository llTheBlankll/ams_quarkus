package com.pshs.ams.app.reports.controllers;

import com.pshs.ams.app.reports.services.ReportService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import lombok.extern.log4j.Log4j2;

@ApplicationScoped
@Log4j2
@Path("/api/v1/reports")
public class ReportController {

	@Inject
	ReportService reportService;

	@Path("/generate/sf2")
	public void generateSF2Report() {
		reportService.generateSF2Report();
	}
}
