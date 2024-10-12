package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.custom.FileUploadInput;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.ClassroomService;
import com.pshs.ams.services.interfaces.TeacherService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

@ApplicationScoped
@Path("/api/v1/uploads")
public class UploadController {

	@Inject
	Logger logger;

	@Inject
	TeacherService teacherService;

	@Inject
	ClassroomService classroomService;

	@POST
	@Path("/teacher/{id}/profile-picture")
	@Consumes("multipart/form-data")
	public Response uploadTeacherProfilePicture(@PathParam("id") Long id, FileUploadInput files) {
		logger.debug("Received upload request for teacher profile picture");
		logger.debug("Teacher ID: " + id);
		if (teacherService.getTeacher(id).isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity(new MessageDTO("Teacher not found", CodeStatus.NOT_FOUND))
					.build();
		}
		// Check if there are files
		if (files.file == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("No files received", CodeStatus.BAD_REQUEST))
					.build();
		}

		// Validate each file
		String contentType = files.file.contentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Only image files are allowed", CodeStatus.BAD_REQUEST))
					.build();
		}

		if (files.file.size() > 8 * 1024 * 1024) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Image size should not exceed 8MB", CodeStatus.BAD_REQUEST))
					.build();
		}

		if (!isValidImageFile(files.file.filePath())) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Invalid image file", CodeStatus.BAD_REQUEST))
					.build();
		}

		// Save the image file
		String secureFileName = UUID.randomUUID() + getFileExtension(files.file.fileName());
		File dest = new File("uploads", secureFileName);
		try (InputStream input = Files.newInputStream(files.file.filePath());
				OutputStream output = Files.newOutputStream(dest.toPath())) {
			input.transferTo(output);
			logger.debug("File moved to uploads folder");
		} catch (IOException e) {
			logger.error("IO error while processing file", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new MessageDTO("IO error while processing file", CodeStatus.FAILED))
					.build();
		}

		// Update the teacher's profile picture in the database
		CodeStatus status = teacherService.uploadTeacherProfilePicture(id, dest.toPath());
		if (status != CodeStatus.OK) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new MessageDTO("Failed to update teacher profile picture", CodeStatus.FAILED))
					.build();
		}

		return Response.ok(new MessageDTO("Teacher profile picture updated successfully", CodeStatus.OK)).build();
	}

	@POST
	@Path("/classroom/{id}/profile-picture")
	@Consumes("multipart/form-data")
	public Response uploadClassroomProfilePicture(@PathParam("id") Long id, FileUploadInput files) {
		logger.debug("Received upload request for classroom profile picture");
		logger.debug("Classroom ID: " + id);

		// Check if the classroom exists
		if (!classroomService.getClassroom(id).isPresent()) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity(new MessageDTO("Classroom not found", CodeStatus.NOT_FOUND))
					.build();
		}

		logger.debug("File: " + (files.file != null ? files.file.fileName() : "null"));
		if (files.file == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("No file received", CodeStatus.BAD_REQUEST))
					.build();
		}

		FileUpload file = files.file;
		logger.debug("File name: " + file.fileName());
		logger.debug("File size: " + file.size());
		logger.debug("File path: " + file.filePath());

		// Validate file type
		String contentType = file.contentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Only image files are allowed", CodeStatus.BAD_REQUEST))
					.build();
		}

		// Validate file size (8MB = 8 * 1024 * 1024 bytes)
		if (file.size() > 8 * 1024 * 1024) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Image size should not exceed 8MB", CodeStatus.BAD_REQUEST))
					.build();
		}

		if (!isValidImageFile(file.filePath())) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(new MessageDTO("Invalid image file", CodeStatus.BAD_REQUEST))
					.build();
		}

		String secureFileName = UUID.randomUUID().toString() + getFileExtension(file.fileName());
		File dest = new File("uploads", secureFileName);
		try {
			try (InputStream input = Files.newInputStream(file.filePath());
					OutputStream output = Files.newOutputStream(dest.toPath())) {
				input.transferTo(output);
				logger.debug("File moved to uploads folder");
			}

			// Update the classroom's profile picture in the database
			CodeStatus status = classroomService.uploadClassroomProfilePicture(id, dest.toPath());
			if (status != CodeStatus.OK) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(new MessageDTO("Failed to update classroom profile picture", CodeStatus.FAILED))
						.build();
			}
		} catch (IOException e) {
			logger.error("IO error while processing file", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(new MessageDTO("IO error while processing file", CodeStatus.FAILED))
					.build();
		} catch (SecurityException e) {
			logger.error("Security error while processing file", e);
			return Response.status(Response.Status.FORBIDDEN)
					.entity(new MessageDTO("Security error while processing file", CodeStatus.FAILED))
					.build();
		}

		return Response.ok(new MessageDTO("Classroom profile picture updated successfully", CodeStatus.OK)).build();
	}

	private boolean isValidImageFile(java.nio.file.Path filePath) {
		try {
			String mimeType = Files.probeContentType(filePath);
			return mimeType != null && mimeType.startsWith("image/");
		} catch (IOException e) {
			logger.error("Error determining file type", e);
			return false;
		}
	}

	private String getFileExtension(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return fileName.substring(lastIndexOf);
	}
}