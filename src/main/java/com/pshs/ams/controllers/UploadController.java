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

import com.pshs.ams.models.entities.Student;
import com.pshs.ams.services.interfaces.StudentService;
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
	StudentService studentService;

	@Inject
	ClassroomService classroomService;

	private static final int MAX_IMAGE_SIZE = 2048; // Maximum width or height in pixels
	private static final float COMPRESSION_QUALITY = 0.85f; // Compression quality (0.0-1.0)
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB in bytes

	@ConfigProperty(name = "upload.directory.teacher", defaultValue = "uploads")
	String TEACHER_UPLOAD_DIRECTORY;

	@ConfigProperty(name = "upload.directory.student", defaultValue = "uploads")
	String STUDENT_UPLOAD_DIRECTORY;

	@ConfigProperty(name = "upload.directory.classrooms", defaultValue = "uploads")
	String CLASSROOM_UPLOAD_DIRECTORY;

	private ExecutorService executorService;

	/**
	 * Initializes the upload controller.
	 * <p>
	 * This method is called after the bean is constructed and all of its dependencies have been injected.
	 * It creates the upload directory if it does not exist and sets up an executor service to be used when
	 * compressing images.
	 */
	@PostConstruct
	public void init() {
		createUploadDirectory();
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	}


	/**
	 * Creates the upload directory if it does not exist.
	 * <p>
	 * If the directory does not exist, this method will log a warning message and attempt to create the
	 * directory. If the directory is created successfully, this method will log an info message. If the
	 * directory cannot be created, this method will log an error message.
	 * <p>
	 * If the directory already exists, this method will log an info message.
	 */
	private void createUploadDirectory() {
		File teacherUploadDir = new File(TEACHER_UPLOAD_DIRECTORY);
		File studentUploadDir = new File(STUDENT_UPLOAD_DIRECTORY);
		File classroomUploadDir = new File(CLASSROOM_UPLOAD_DIRECTORY);
		if (!teacherUploadDir.exists() || !studentUploadDir.exists() || !studentUploadDir.exists()) {
			logger.warn("Upload directory does not exist: " + teacherUploadDir.getAbsolutePath());
			if (teacherUploadDir.mkdirs() && studentUploadDir.mkdirs() && classroomUploadDir.mkdirs()) {
				logger.info("Created upload directory: " + teacherUploadDir.getAbsolutePath());
			} else {
				logger.error("Failed to create upload directory: " + teacherUploadDir.getAbsolutePath());
			}
		} else {
			logger.info("Upload directory exists: " + teacherUploadDir.getAbsolutePath());
		}
	}

	/**
	 * Compresses an image and saves it to a new file if needed.
	 * If the image is larger than the maximum image size, it is resized to fit within the maximum size. The image is also
	 * compressed using the specified compression quality.
	 *
	 * @param inputFile The input image file
	 * @param fileName  The name of the input file
	 * @return The compressed image file
	 * @throws IOException If there is an error reading or writing the image file
	 */
	private File compressImage(File inputFile, String fileName, String savePath) throws IOException {
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
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(getFileExtension(fileName).substring(1));
		ImageWriter writer = writers.next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
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
		File compressedFile = new File(savePath, "compressed_" + fileName);
		logger.debug("Saving compressed image");
		try (OutputStream os = Files.newOutputStream(
			compressedFile.toPath(), StandardOpenOption.CREATE,
			StandardOpenOption.WRITE
		)
		) {
			outputStream.writeTo(os);
		}

		return compressedFile;
	}

	@POST
	@Path("/teacher/{id}/profile-picture")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional
	public CompletableFuture<Response> uploadTeacherProfilePicture(@PathParam("id") Long id, FileUploadInput fileInput) {
		return CompletableFuture.supplyAsync(
			() -> QuarkusTransaction.requiringNew().call(() -> {
				logger.debug("Received upload request for teacher profile picture. Teacher ID: " + id);

				Optional<Teacher> teacherOptional = teacherService.getTeacher(id);
				if (teacherOptional.isEmpty()) {
					return createErrorResponse(Response.Status.NOT_FOUND, "Teacher not found", CodeStatus.NOT_FOUND);
				}

				if (fileInput.file == null) {
					logger.debug("Upload request failed. No file was received.");
					return createErrorResponse(Response.Status.BAD_REQUEST, "No files received", CodeStatus.BAD_REQUEST);
				}

				if (isImageFile(fileInput.file.filePath(), fileInput.file.contentType())) {
					return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid image file", CodeStatus.BAD_REQUEST);
				}

				try {
					String secureFileName = generateSecureFileName(fileInput.file.fileName());
					File dest = new File(TEACHER_UPLOAD_DIRECTORY, secureFileName);
					saveUploadedFile(fileInput.file, dest);

					File compressedFile = compressImage(dest, secureFileName, TEACHER_UPLOAD_DIRECTORY);

					Teacher teacher = teacherOptional.get();
					deleteProfilePicture(teacher);

					CodeStatus status = teacherService.uploadTeacherProfilePicture(id, compressedFile.toPath());
					if (status != CodeStatus.OK) {
						return createErrorResponse(
							Response.Status.INTERNAL_SERVER_ERROR, "Failed to update teacher profile picture",
							CodeStatus.FAILED
						);
					}

					deleteOriginalFile(dest, compressedFile);

					return Response.ok(new MessageDTO("Teacher profile picture updated successfully", CodeStatus.OK)).build();
				} catch (IOException e) {
					logger.error("IO error while processing file", e);
					return createErrorResponse(
						Response.Status.INTERNAL_SERVER_ERROR, "IO error while processing file",
						CodeStatus.FAILED
					);
				}
			}), executorService
		);
	}

	/**
	 * Uploads a profile picture for a student.
	 *
	 * @param id The id of the student to upload the profile picture for
	 * @param fileInput The uploaded file
	 * @return A response indicating the status of the operation
	 */
	@POST
	@Path("/student/{id}/profile-picture")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional
	public CompletableFuture<Response> uploadStudentProfilePicture(@PathParam("id") Long id, FileUploadInput fileInput) {
		return CompletableFuture.supplyAsync(
			() -> QuarkusTransaction.requiringNew().call(() -> {
				logger.debug("Received upload request for student profile picture. student ID: " + id);

				Optional<Student> studentOptional = this.studentService.getStudent(id);
				if (studentOptional.isEmpty()) {
					return createErrorResponse(Response.Status.NOT_FOUND, "Teacher not found", CodeStatus.NOT_FOUND);
				}

				if (fileInput.file == null) {
					logger.debug("Upload request failed. No file was received.");
					return createErrorResponse(Response.Status.BAD_REQUEST, "No files received", CodeStatus.BAD_REQUEST);
				}

				if (isImageFile(fileInput.file.filePath(), fileInput.file.contentType())) {
					return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid image file", CodeStatus.BAD_REQUEST);
				}

				try {
					String secureFileName = generateSecureFileName(fileInput.file.fileName());
					File dest = new File(STUDENT_UPLOAD_DIRECTORY, secureFileName);
					saveUploadedFile(fileInput.file, dest);

					File compressedFile = compressImage(dest, secureFileName, STUDENT_UPLOAD_DIRECTORY);

					Student student = studentOptional.get();

					// Delete the existing profile picture to prevent duplicates and save space.
					deleteProfilePicture(student);

					// Update the profile picture
					CodeStatus status = this.studentService.uploadStudentProfilePicture(id, compressedFile.toPath());
					if (status != CodeStatus.OK) {
						return createErrorResponse(
							Response.Status.INTERNAL_SERVER_ERROR, "Failed to update student profile picture",
							CodeStatus.FAILED
						);
					}

					deleteOriginalFile(dest, compressedFile);
					return Response.ok(new MessageDTO("Student profile picture updated successfully", CodeStatus.OK)).build();
				} catch (IOException e) {
					logger.error("IO error while processing file", e);
					return createErrorResponse(
						Response.Status.INTERNAL_SERVER_ERROR, "IO error while processing file",
						CodeStatus.FAILED
					);
				}
			}), executorService
		);
	}


	/**
	 * Generates a secure file name by appending a random UUID to the file extension
	 * of the original file name.
	 *
	 * @param originalFileName the name of the original file
	 * @return a unique, secure file name
	 */
	private String generateSecureFileName(String originalFileName) {
		return UUID.randomUUID() + getFileExtension(originalFileName);
	}

	/**
	 * Saves the uploaded file to the specified destination. This method will
	 * delete the temporary file after it has been copied to the destination.
	 *
	 * @param file     the uploaded file
	 * @param destination the destination file
	 * @throws IOException if an I/O error occurs
	 */
	private void saveUploadedFile(FileUpload file, File destination) throws IOException {
		try (InputStream input = Files.newInputStream(file.filePath());
		     OutputStream output = Files.newOutputStream(destination.toPath())
		) {
			input.transferTo(output);
			logger.debug("File moved to " + destination.getPath() + " folder");
		}
	}

	/**
	 * Deletes the existing profile picture for the given teacher, if any.
	 *
	 * @param teacher the teacher to delete the existing profile picture for
	 */
	private void deleteProfilePicture(Teacher teacher) {
		String profile = teacher.getProfilePicture();
		logger.debug("Existing profile picture: " + profile);

		if (profile != null && !profile.isEmpty()) {
			java.nio.file.Path existingFilePath = java.nio.file.Paths.get(profile);
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

	/**
	 * Deletes the existing profile picture for the given student, if any.
	 * <p>
	 * This method checks if the student has an existing profile picture.
	 * If a profile picture exists, it attempts to delete the file from the file
	 * system. Logs information regarding the success or failure of the deletion.
	 *
	 * @param student the student to delete the existing profile picture for
	 */
	private void deleteProfilePicture(Student student) {
		String profilePicture = student.getProfilePicture();
		logger.debug("Existing profile picture: " + profilePicture);

		if (profilePicture != null && !profilePicture.isEmpty()) {
			java.nio.file.Path existingFilePath = java.nio.file.Paths.get(profilePicture);
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


	/**
	 * Deletes the original file used for compression, if it is different from the
	 * compressed file.
	 * <p>
	 * This method takes two parameters, the original file and the compressed file.
	 * If the two files are different, it attempts to delete the original file
	 * from the file system. It will log information regarding the success or
	 * failure of the deletion.
	 *
	 * @param originalFile   the original file used for compression
	 * @param compressedFile the compressed file
	 * @throws IOException if an I/O error occurs
	 */
	private void deleteOriginalFile(File originalFile, File compressedFile) throws IOException {
		if (!compressedFile.equals(originalFile)) {
			Files.delete(originalFile.toPath());
		}
	}

	/**
	 * Uploads a profile picture for a classroom.
	 * <p>
	 * This API endpoint accepts a {@code FileUploadInput} object containing a
	 * {@code FileUpload} object. It validates the input, checks if the received
	 * file is an image file, and saves it to the specified directory. Then it
	 * compresses the image and updates the classroom profile picture.
	 * <p>
	 * If the classroom is not found, it returns a 404 response. If the input is
	 * invalid or if the received file is not an image file, it returns a 400
	 * response. If an I/O error occurs while processing the file, it returns a
	 * 500 response.
	 *
	 * @param id the id of the classroom to upload the profile picture for
	 * @param fileInput a {@code FileUploadInput} object containing a
	 *                  {@code FileUpload} object
	 * @return a {@code CompletableFuture} containing a {@code Response} object
	 *         indicating the result of the operation
	 */
	@POST
	@Path("/classroom/{id}/profile-picture")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Transactional
	public CompletableFuture<Response> uploadClassroomProfilePicture(
		@PathParam("id") Long id,
		FileUploadInput fileInput
	) {
		return CompletableFuture.supplyAsync(
			() -> QuarkusTransaction.requiringNew().call(() -> {
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

				if (isImageFile(file.filePath(), file.contentType())) {
					logger.debug("Invalid image file");
					return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid image file", CodeStatus.BAD_REQUEST);
				}

				try {
					String secureFileName = generateSecureFileName(file.fileName());
					File dest = new File(CLASSROOM_UPLOAD_DIRECTORY, secureFileName);
					saveUploadedFile(file, dest);

					File compressedFile = compressImage(dest, secureFileName, CLASSROOM_UPLOAD_DIRECTORY);

					CodeStatus status = classroomService.uploadClassroomProfilePicture(id, compressedFile.toPath());
					if (status != CodeStatus.OK) {
						logger.error("Failed to update classroom profile picture");
						return createErrorResponse(
							Response.Status.INTERNAL_SERVER_ERROR,
							"Failed to update classroom profile picture", CodeStatus.FAILED
						);
					}

					deleteOriginalFile(dest, compressedFile);
					return Response.ok(new MessageDTO("Classroom profile picture updated successfully", CodeStatus.OK)).build();
				} catch (IOException e) {
					logger.error("IO error while processing file", e);
					return createErrorResponse(
						Response.Status.INTERNAL_SERVER_ERROR, "IO error while processing file",
						CodeStatus.FAILED
					);
				}
			}), executorService
		);
	}

	// Region: GET PROFILE PICTURES

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

	@GET
	@Path("/student/{id}/profile-picture")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getStudentProfilePicture(@PathParam("id") Long id) {
		logger.debug("Received request to get student profile picture. Teacher ID: " + id);
		Optional<Student> student = studentService.getStudent(id);
		if (student.isEmpty()) {
			logger.debug("Student not found");
			return createErrorResponse(Response.Status.NOT_FOUND, "Teacher not found", CodeStatus.NOT_FOUND);
		}

		String profilePicturePath = student.get().getProfilePicture();
		if (profilePicturePath == null || profilePicturePath.isEmpty()) {
			logger.debug("Profile picture not found");
			return createErrorResponse(Response.Status.NOT_FOUND, "Profile picture not found", CodeStatus.NOT_FOUND);
		}

		return getProfilePictureResponse(profilePicturePath);
	}

	/**
	 * Creates a response containing a profile picture from the given path.
	 *
	 * @param profilePicturePath the path to the profile picture
	 * @return a response containing the profile picture
	 */
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
		return switch (extension) {
			case ".jpg", ".jpeg" -> "image/jpeg";
			case ".png" -> "image/png";
			case ".gif" -> "image/gif";
			case ".bmp" -> "image/bmp";
			default -> "application/octet-stream";
		};
	}

	private boolean isImageFile(java.nio.file.Path filePath, String contentType) {
		if (contentType == null || !contentType.startsWith("image/")) {
			return true;
		}

		try (InputStream is = Files.newInputStream(filePath)) {
			byte[] magic = new byte[8];
			if (is.read(magic) != magic.length) {
				logger.debug("Unable to read file header");
				return true;
			}

			if (isValidImageFormat(magic)) {
				return !isImageReadable(filePath);
			}

			logger.debug("Invalid image file format");
			return true;
		} catch (IOException e) {
			logger.error("Error reading file", e);
			return true;
		}
	}

	private boolean isValidImageFormat(byte[] magic) {
		return isJPEG(magic) || isPNG(magic) || isGIF(magic) || isBMP(magic);
	}

	private boolean isImageReadable(java.nio.file.Path filePath) {
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
