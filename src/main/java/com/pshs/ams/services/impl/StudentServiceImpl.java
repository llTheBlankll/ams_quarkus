package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Classroom;
import com.pshs.ams.models.entities.Student;
import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.dto.strand.MostPopularStrandDTO;
import com.pshs.ams.services.interfaces.ClassroomService;
import com.pshs.ams.services.interfaces.StudentService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class StudentServiceImpl implements StudentService {

	@Inject
	Logger logger;

	@Inject
	ClassroomService classroomService;

	/**
	 * Retrieves a list of all students with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy,
	 *             sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Student objects
	 */
	@Override
	public List<Student> getAllStudents(Sort sort, Page page) {
		logger.debug("Get all students: " + sort + ", " + page);
		return Student.findAll(sort).page(page).list();
	}

	/**
	 * Creates a new student.
	 *
	 * @param student the Student to create
	 * @return the created Student
	 */
	@Override
	@Transactional
	public CodeStatus createStudent(Student student) {
		if (student == null) {
			logger.debug("Student is null");
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Student> existingStudent = Student.find("id", student.getId()).firstResultOptional();
		if (existingStudent.isPresent()) {
			logger.debug("Student already exists");
			return CodeStatus.EXISTS;
		}

		student.persist();
		logger.debug("Student created: " + student);
		return CodeStatus.OK;
	}

	/**
	 * Deletes the student with the given id.
	 *
	 * @param id the id of the student to delete
	 * @return the status of the delete operation
	 */
	@Override
	@Transactional
	public CodeStatus deleteStudent(Long id) {
		if (id <= 0) {
			logger.debug("Invalid id: " + id);
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Student> existingStudent = Student.findByIdOptional(id);
		if (existingStudent.isPresent()) {
			existingStudent.get().delete();
			logger.debug("Student deleted: " + id);
			return CodeStatus.OK;
		}

		logger.debug("Student not found: " + id);
		return CodeStatus.NOT_FOUND;
	}

	/**
	 * Retrieves the total number of students.
	 *
	 * @return the total number of students
	 */
	@Override
	public long getTotalStudents() {
		return Student.count();
	}

	/**
	 * Retrieves the total number of students in the given classroom.
	 *
	 * @param classroomId the id of the classroom
	 * @return the total number of students in the given classroom
	 */
	@Override
	public long getTotalStudents(Long classroomId) {
		// Check if exists
		if (classroomId <= 0) {
			logger.debug("Invalid classroom id: " + classroomId);
			return 0;
		}

		// Check if the classroom exists
		Optional<Classroom> classroom = classroomService.getClassroom(classroomId);
		if (classroom.isEmpty()) {
			logger.debug("Classroom not found: " + classroomId);
			return 0;
		}

		// Check if exists
		return classroom.get().getStudents().size();
	}

	/**
	 * Retrieves the student with the given id.
	 *
	 * @param id the id of the student to retrieve
	 * @return the retrieved Student
	 */
	@Override
	public Optional<Student> getStudent(Long id) {
		if (id <= 0) {
			logger.debug("Invalid id: " + id);
			return Optional.empty();
		}

		// Return the student
		return Student.findByIdOptional(id);
	}

	@Override
	public CodeStatus assignClassroomToStudent(Long id, Long classroomId) {
		// Check if exists
		if (id <= 0 || classroomId <= 0) {
			logger.debug("Invalid id: " + id + " or classroom id: " + classroomId);
			return CodeStatus.BAD_REQUEST;
		}

		// Check if the student exists
		Optional<Student> student = getStudent(id);
		if (student.isEmpty()) {
			logger.debug("Student not found: " + id);
			return CodeStatus.NOT_FOUND;
		}

		// Check if the classroom exists
		Optional<Classroom> classroom = classroomService.getClassroom(classroomId);
		if (classroom.isEmpty()) {
			logger.debug("Classroom not found: " + classroomId);
			return CodeStatus.NOT_FOUND;
		}

		// Assign the classroom to the student
		student.get().setClassroom(classroom.get());
		student.get().persist();
		return CodeStatus.OK;
	}

	/**
	 * Uploads a profile picture for the student with the given id.
	 *
	 * @param id   the id of the student to upload the profile picture for
	 * @param path the path to the profile picture
	 * @return the status of the upload operation
	 */
	@Override
	@Transactional
	public CodeStatus uploadStudentProfilePicture(Long id, Path path) {
		Optional<Student> studentOptional = getStudent(id);

		if (studentOptional.isPresent()) {
			if (Student.update("profilePicture = ?1 WHERE id = ?2", path.toString(), id) > 0) {
				return CodeStatus.OK;
			} else {
				return CodeStatus.FAILED;
			}
		}

		return CodeStatus.NOT_FOUND;
	}

	@Override
	public List<Student> searchStudentByName(String name, Sort sort, Page page) {
		return Student.find("firstName LIKE ?1 OR lastName LIKE ?1", Sort.by("lastName"), "%" + name + "%").page(page)
				.list();
	}

	@Override
	public long getStudentCountByStrand(Long strandId) {
		return Student.count("strand.id", strandId);
	}

	@Override
	public long getStudentCountByGradeLevel(Long gradeLevelId) {
		return Student.count("gradeLevel.id", gradeLevelId);
	}

	@Override
	public Optional<MostPopularStrandDTO> getMostPopularStrand() {
		List<Object[]> result = Strand.find("SELECT s.id, s.name, COUNT(st) as studentCount " +
				"FROM Strand s LEFT JOIN Student st ON st.strand = s " +
				"GROUP BY s.id, s.name " +
				"ORDER BY studentCount DESC")
				.project(Object[].class)
				.page(Page.ofSize(1))
				.list();

		if (result.isEmpty()) {
			return Optional.empty();
		}

		Object[] mostPopular = result.get(0);
		return Optional.of(new MostPopularStrandDTO(
			(Integer) mostPopular[0],
			(String) mostPopular[1],
			((Number) mostPopular[2]).longValue()
		));
	}

	@Override
	public double getAverageStudentsPerStrand() {
		Long totalStudents = Student.count();
		Long totalStrands = Strand.count();
		return totalStrands == 0 ? 0 : (double) totalStudents / totalStrands;
	}

	@Override
	public LineChartDTO getStrandDistribution(LocalDate startDate, LocalDate endDate) {
		// Convert the LocalDate to LocalDateTime
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

		List<Object[]> results = Student.find("SELECT s.name, COUNT(st) " +
				"FROM Student st JOIN st.strand s " +
				"WHERE st.createdAt BETWEEN ?1 AND ?2 " +
				"GROUP BY s.name " +
				"ORDER BY s.name", startDateTime, endDateTime)
				.project(Object[].class)
				.list();

		List<String> labels = results.stream()
				.map(r -> (String) r[0])
				.collect(Collectors.toList());

		List<String> data = results.stream()
				.map(r -> String.valueOf((Long) r[1]))
				.collect(Collectors.toList());

		return new LineChartDTO(labels, data);
	}
}
