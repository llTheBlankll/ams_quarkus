package com.pshs.ams.controllers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.pshs.ams.models.dto.custom.FileUploadInput;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.entities.Classroom;
import com.pshs.ams.models.entities.Teacher;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.ClassroomService;
import com.pshs.ams.services.interfaces.TeacherService;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/api/v1/uploads")
public class UploadController {

	@Inject
	Logger logger;

	@Inject
	TeacherService teacherService;

	@Inject
	ClassroomService classroomService;

	private static final int MAX_IMAGE_SIZE = 2048; // Maximum width or height in pixels
	private static final float COMPRESSION_QUALITY = 0.85f; // Compression quality (0.0-1.0)
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB in bytes

	@ConfigProperty(name = "upload.directory", defaultValue = "uploads")
	String UPLOAD_DIRECTORY;

	private ExecutorService executorService;

	@PostConstruct
	public void init() {
		createUploadDirectoryIfNotExists();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}

	private void createUploadDirectoryIfNotExists() {
		File uploadDir = new File(UPLOAD_DIRECTORY);
		if (!uploadDir.exists()) {
			logger.warn("Upload directory does not exist: " + uploadDir.getAbsolutePath());
			if (uploadDir.mkdirs()) {
				logger.info("Created upload directory: " + uploadDir.getAbsolutePath());
			} else {
				logger.error("Failed to create upload directory: " + uploadDir.getAbsolutePath());
			}
		} else {
			logger.info("Upload directory exists: " + uploadDir.getAbsolutePath());
		}
	}

	private File compressImageIfNeeded(File inputFile, String fileName) throws IOException {
		BufferedImage image = ImageIO.read(inputFile);
		if (image == null) {
			throw new IOException("Failed to read image file");
		}

		int width = image.getWidth();
		int height = image.getHeight();
		boolean needsCompression = inputFile.length() > MAX_FILE_SIZE;

		// Check if resizing is needed
		if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE || needsCompression) {
			logger.debug("Image needs compression");
			float scale = Math.min((float) MAX_IMAGE_SIZE / width, (float) MAX_IMAGE_SIZE / height);
			int newWidth = Math.round(width * scale);
			int newHeight = Math.round(height * scale);

			BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(image, 0, 0, newWidth, newHeight, null);
			g.dispose();

			image = resizedImage;
		}

		// Compress the image
		logger.debug("Compressing image");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(getFileExtension(fileName).substring(1));
		ImageWriter writer = writers.next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
		writer.setOutput(ios);

		ImageWriteParam param = writer.getDefaultWriteParam();
		if (param.canWriteCompressed()) {
			logger.debug("Setting compression mode to explicit");
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(COMPRESSION_QUALITY);
		}

		logger.debug("Writing image");
		writer.write(null, new IIOImage(image, null, null), param);
		writer.dispose();

		// Save the compressed image
		File compressedFile = new File(UPLOAD_DIRECTORY, "compressed_" + fileName);
		logger.debug("Saving compressed image");
		try (OutputStream os = Files.newOutputStream(compressedFile.toPath(), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE)) {
			baos.writeTo(os);
		}

		return compressedFile;
	}

	@POST
	@Path("/teacher/{id}/profile-picture")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional
	public CompletableFuture<Response> uploadTeacherProfilePicture(@PathParam("id") Long id, FileUploadInput fileInput) {
		return CompletableFuture.supplyAsync(() -> QuarkusTransaction.requiringNew().call(() -> {
			logger.debug("Received upload request for teacher profile picture. Teacher ID: " + id);

			Optional<Teacher> teacherOptional = teacherService.getTeacher(id);
			if (teacherOptional.isEmpty()) {
				return createErrorResponse(Response.Status.NOT_FOUND, "Teacher not found", CodeStatus.NOT_FOUND);
			}

			if (fileInput.file == null) {
				logger.debug("Upload request failed. No file was received.");
				return createErrorResponse(Response.Status.BAD_REQUEST, "No files received", CodeStatus.BAD_REQUEST);
			}

			if (!isValidImageFile(fileInput.file.filePath(), fileInput.file.contentType())) {
				return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid image file", CodeStatus.BAD_REQUEST);
			}

			try {
				String secureFileName = generateSecureFileName(fileInput.file.fileName());
				File dest = new File(UPLOAD_DIRECTORY, secureFileName);
				saveUploadedFile(fileInput.file, dest);

				File compressedFile = compressImageIfNeeded(dest, secureFileName);

				Teacher teacher = teacherOptional.get();
				deleteExistingProfilePicture(teacher);

				CodeStatus status = teacherService.uploadTeacherProfilePicture(id, compressedFile.toPath());
				if (status != CodeStatus.OK) {
					return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Failed to update teacher profile picture",
							CodeStatus.FAILED);
				}

				deleteOriginalFileIfCompressed(dest, compressedFile);

				return Response.ok(new MessageDTO("Teacher profile picture updated successfully", CodeStatus.OK)).build();
			} catch (IOException e) {
				logger.error("IO error while processing file", e);
				return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "IO error while processing file",
						CodeStatus.FAILED);
			}
		}), executorService);
	}

	private String generateSecureFileName(String originalFileName) {
		return UUID.randomUUID() + getFileExtension(originalFileName);
	}

	private void saveUploadedFile(FileUpload file, File destination) throws IOException {
		try (InputStream input = Files.newInputStream(file.filePath());
				OutputStream output = Files.newOutputStream(destination.toPath())) {
			input.transferTo(output);
			logger.debug("File moved to uploads folder");
		}
	}

	private void deleteExistingProfilePicture(Teacher teacher) {
		String existingProfilePicture = teacher.getProfilePicture();
		logger.debug("Existing profile picture: " + existingProfilePicture);

		if (existingProfilePicture != null && !existingProfilePicture.isEmpty()) {
			java.nio.file.Path existingFilePath = java.nio.file.Paths.get(existingProfilePicture);
			logger.debug("Full path of existing profile picture: " + existingFilePath.toAbsolutePath());

			try {
				boolean deleted = Files.deleteIfExists(existingFilePath);
				if (deleted) {
					logger.info("Successfully deleted existing profile picture: " + existingFilePath);
				} else {
					logger.warn("Existing profile picture file not found: " + existingFilePath);
				}
			} catch (IOException e) {
				logger.error("Error deleting existing profile picture: {}", existingFilePath, e);
			}
		} else {
			logger.debug("No existing profile picture to delete");
		}
	}

	private void deleteOriginalFileIfCompressed(File originalFile, File compressedFile) throws IOException {
		if (!compressedFile.equals(originalFile)) {
			Files.delete(originalFile.toPath());
		}
	}

	@POST
	@Path("/classroom/{id}/profile-picture")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional
	public CompletableFuture<Response> uploadClassroomProfilePicture(@PathParam("id") Long id,
			FileUploadInput fileInput) {
		return CompletableFuture.supplyAsync(() -> {
			return QuarkusTransaction.requiringNew().call(() -> {
				logger.debug("Received upload request for classroom profile picture. Classroom ID: " + id);
				logger.debug("FileUploadInput: " + fileInput);

				Optional<Classroom> classroomOptional = classroomService.getClassroom(id);
				if (classroomOptional.isEmpty()) {
					logger.debug("Classroom not found");
					return createErrorResponse(Response.Status.NOT_FOUND, "Classroom not found", CodeStatus.NOT_FOUND);
				}

				if (fileInput == null) {
					logger.debug("FileUploadInput is null");
					return createErrorResponse(Response.Status.BAD_REQUEST, "No input received", CodeStatus.BAD_REQUEST);
				}

				if (fileInput.file == null) {
					logger.debug("No file received in FileUploadInput");
					return createErrorResponse(Response.Status.BAD_REQUEST, "No file received", CodeStatus.BAD_REQUEST);
				}

				FileUpload file = fileInput.file;
				logger.debug("Received file: " + file.fileName() + ", Content-Type: " + file.contentType());

				if (!isValidImageFile(file.filePath(), file.contentType())) {
					logger.debug("Invalid image file");
					return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid image file", CodeStatus.BAD_REQUEST);
				}

				try {
					String secureFileName = generateSecureFileName(file.fileName());
					File dest = new File(UPLOAD_DIRECTORY, secureFileName);
					saveUploadedFile(file, dest);

					File compressedFile = compressImageIfNeeded(dest, secureFileName);

					CodeStatus status = classroomService.uploadClassroomProfilePicture(id, compressedFile.toPath());
					if (status != CodeStatus.OK) {
						logger.error("Failed to update classroom profile picture");
						return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
								"Failed to update classroom profile picture", CodeStatus.FAILED);
					}

					deleteOriginalFileIfCompressed(dest, compressedFile);
					return Response.ok(new MessageDTO("Classroom profile picture updated successfully", CodeStatus.OK)).build();
				} catch (IOException e) {
					logger.error("IO error while processing file", e);
					return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "IO error while processing file",
							CodeStatus.FAILED);
				}
			});
		}, executorService);
	}

	@GET
	@Path("/classroom/{id}/profile-picture")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getClassroomProfilePicture(@PathParam("id") Long id) {
		logger.debug("Received request to get classroom profile picture. Classroom ID: " + id);
		Optional<Classroom> classroom = classroomService.getClassroom(id);
		if (classroom.isEmpty()) {
			logger.debug("Classroom not found");
			return createErrorResponse(Response.Status.NOT_FOUND, "Classroom not found", CodeStatus.NOT_FOUND);
		}

		String profilePicturePath = classroom.get().getProfilePicture();
		if (profilePicturePath == null || profilePicturePath.isEmpty()) {
			logger.debug("Profile picture not found");
			return createErrorResponse(Response.Status.NOT_FOUND, "Profile picture not found", CodeStatus.NOT_FOUND);
		}

		return getProfilePictureResponse(profilePicturePath);
	}

	@GET
	@Path("/teacher/{id}/profile-picture")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getTeacherProfilePicture(@PathParam("id") Long id) {
		logger.debug("Received request to get teacher profile picture. Teacher ID: " + id);
		Optional<Teacher> teacher = teacherService.getTeacher(id);
		if (teacher.isEmpty()) {
			logger.debug("Teacher not found");
			return createErrorResponse(Response.Status.NOT_FOUND, "Teacher not found", CodeStatus.NOT_FOUND);
		}

		String profilePicturePath = teacher.get().getProfilePicture();
		if (profilePicturePath == null || profilePicturePath.isEmpty()) {
			logger.debug("Profile picture not found");
			return createErrorResponse(Response.Status.NOT_FOUND, "Profile picture not found", CodeStatus.NOT_FOUND);
		}

		return getProfilePictureResponse(profilePicturePath);
	}

	private Response getProfilePictureResponse(String profilePicturePath) {
		File file = new File(profilePicturePath);
		if (!file.exists()) {
			return createErrorResponse(Response.Status.NOT_FOUND, "Profile picture file not found", CodeStatus.NOT_FOUND);
		}

		String fileName = file.getName();
		String contentType = getContentType(fileName);

		return Response.ok(file)
				.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
				.header("Content-Type", contentType)
				.build();
	}

	private Response createErrorResponse(Response.Status status, String message, CodeStatus codeStatus) {
		return Response.status(status)
				.entity(new MessageDTO(message, codeStatus))
				.build();
	}

	private String getContentType(String fileName) {
		String extension = getFileExtension(fileName).toLowerCase();
		switch (extension) {
			case ".jpg":
			case ".jpeg":
				return "image/jpeg";
			case ".png":
				return "image/png";
			case ".gif":
				return "image/gif";
			case ".bmp":
				return "image/bmp";
			default:
				return "application/octet-stream";
		}
	}

	private boolean isValidImageFile(java.nio.file.Path filePath, String contentType) {
		if (contentType == null || !contentType.startsWith("image/")) {
			return false;
		}

		try (InputStream is = Files.newInputStream(filePath)) {
			byte[] magic = new byte[8];
			if (is.read(magic) != magic.length) {
				logger.debug("Unable to read file header");
				return false;
			}

			if (isValidImageFormat(magic)) {
				return isReadableImage(filePath);
			}

			logger.debug("Invalid image file format");
			return false;
		} catch (IOException e) {
			logger.error("Error reading file", e);
			return false;
		}
	}

	private boolean isValidImageFormat(byte[] magic) {
		return isJPEG(magic) || isPNG(magic) || isGIF(magic) || isBMP(magic);
	}

	private boolean isReadableImage(java.nio.file.Path filePath) {
		try {
			BufferedImage image = ImageIO.read(filePath.toFile());
			if (image == null) {
				logger.debug("File is not a valid image");
				return false;
			}
			logger.debug("Valid image file detected");
			return true;
		} catch (IOException e) {
			logger.debug("Error reading image file", e);
			return false;
		}
	}

	private boolean isJPEG(byte[] magic) {
		return (magic[0] & 0xFF) == 0xFF && (magic[1] & 0xFF) == 0xD8;
	}

	private boolean isPNG(byte[] magic) {
		return magic[0] == (byte) 0x89 && magic[1] == 'P' && magic[2] == 'N' && magic[3] == 'G';
	}

	private boolean isGIF(byte[] magic) {
		return magic[0] == 'G' && magic[1] == 'I' && magic[2] == 'F' && magic[3] == '8';
	}

	private boolean isBMP(byte[] magic) {
		return magic[0] == 'B' && magic[1] == 'M';
	}

	private String getFileExtension(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return fileName.substring(lastIndexOf);
	}
}
