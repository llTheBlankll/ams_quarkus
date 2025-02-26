package com.pshs.ams.app.fingerprints.controllers;

import com.pshs.ams.app.attendances.models.enums.AttendanceMode;
import com.pshs.ams.app.attendances.services.AttendanceService;
import com.pshs.ams.app.fingerprints.models.AttendanceFileUpload;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.app.fingerprints.services.FingerprintService;
import com.pshs.ams.global.models.custom.RFIDCard;
import com.pshs.ams.global.models.enums.CodeStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Path("/api")
public class FingerprintController {

	@Inject
	FingerprintService fingerprintService;

	@Inject
	AttendanceService attendanceService;

	@Inject
	Logger log;

	@GET
	@Path("/fingerprint/{id}")
	public Response enrollFingerprint(@PathParam("id") Integer fingerprintId, @QueryParam("mode") String mode) {
		log.debug("Enrolling fingerprint with ID: " + fingerprintId);

		Optional<MessageResponse> messageDTO = fingerprintService.enrollFingerprint(fingerprintId);
		if (messageDTO.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		return Response.ok(messageDTO.get()).build();
	}

	@POST
	@Path("/fingerprint/upload-attendances")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadAttendanceFile(AttendanceFileUpload form) {
		try {
			log.debug("Attendance file upload requested.");
			if (form != null && form.file != null) {
				// Process CSV file
				log.debug("Processing attendance file...");
				processAttendanceFile(form.file);
				log.debug("File uploaded successfully.");
				return Response.ok("File uploaded successfully").build();
			}

			log.debug("Invalid input");
			throw new IllegalArgumentException("Invalid Input.");
		} catch (IOException e) {
			log.error(e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
				.entity(new MessageResponse(
					"File can't be opened",
					CodeStatus.FAILED
				)).build();
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			return Response.status(Response.Status.BAD_REQUEST).entity(
				new MessageResponse(
					"Invalid Input",
					CodeStatus.BAD_REQUEST
				)).build();
		}
	}

	private void processAttendanceFile(InputStream fileInputStream) throws IOException {
		// Implement your CSV processing logic here
		// Example: Parse CSV, save to database, etc.
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
			// Define a formatter that handles dates without leading zeros in month, day, and seconds
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");

			String line;
			while ((line = reader.readLine()) != null) {
				log.debug(line);
				// Read CSV data and save to database
				String[] values = line.split(",");
				try {
					int fingerprintId = Integer.parseInt(values[0]);
					// Use the formatter to parse the date string
					LocalDateTime dateTime = LocalDateTime.parse(values[1], formatter);
					AttendanceMode mode = AttendanceMode.valueOf(values[2]);
					log.debug("Enrolling fingerprint with ID: " + fingerprintId + ", date: " + dateTime + ", mode: " + mode);
				} catch (Exception e) {
					log.error("Error parsing line: " + line + " - " + e.getMessage(), e);
				}
			}
		}
	}
}
