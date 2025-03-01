package com.pshs.ams.app.classrooms.impl;

import com.pshs.ams.app.classrooms.exceptions.ClassroomExistsException;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.classrooms.services.ClassroomService;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.global.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.jboss.logging.Logger;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClassroomServiceImpl implements ClassroomService {

	@Inject
	Logger logger;

	/**
	 * Retrieves a list of all classes with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy,
	 *             sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Classroom objects
	 */
	@Override
	public List<Classroom> listAll(Sort sort, Page page) {
		logger.debug("Get all classes: " + sort + ", " + page);
		return Classroom.findAll(sort).page(page).list();
	}

	/**
	 * Creates a new class.
	 *
	 * @param classroom a Student object
	 * @return the created Student object
	 */
	@Override
	@Transactional
	public Optional<Classroom> create(Classroom classroom) throws IllegalArgumentException, ClassroomExistsException {
		if (classroom == null) {
			throw new IllegalArgumentException("Classroom cannot be null.");
		}

		Optional<Classroom> existingClass = Classroom.find("classroomName", classroom.getClassroomName())
			.firstResultOptional();
		if (existingClass.isPresent()) {
			logger.debug("Class already exists: " + existingClass.get().getClassroomName());
			return Optional.empty();
		}

		classroom.persistAndFlush();
		Classroom.getEntityManager().refresh(classroom); // Get the persisted object
		logger.debug("Class created: " + classroom.getClassroomName());
		return Optional.of(classroom);
	}

	/**
	 * Deletes the class with the given id.
	 *
	 * @param id the id of the class to delete
	 */
	@Override
	@Transactional
	public void delete(Integer id) throws ClassroomExistsException, IllegalArgumentException {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Invalid id: " + id);
		}

		logger.debug("Deleting Class: " + id);
		Optional<Classroom> existingClass = Classroom.findByIdOptional(id);
		if (existingClass.isPresent()) {
			existingClass.get().delete();
			logger.debug("Class deleted: " + existingClass.get().getClassroomName());
			return;
		}

		logger.debug("Class not found: " + id);
		throw new ClassroomExistsException("Class not found: " + id);
	}

	/**
	 * Retrieves the class with the given id.
	 *
	 * @param id the id of the class to retrieve
	 * @return the retrieved Classroom object
	 */
	@Override
	public Optional<Classroom> get(Long id) {
		logger.debug("Get Class: " + id);
		return Classroom.findByIdOptional(id);
	}

	/**
	 * Assigns a list of students to a specified classroom.
	 *
	 * @param classroomId the id of the classroom to which the students will be assigned
	 * @param students    a list of Student objects to be assigned to the classroom
	 * @return a list of Student objects that have been successfully assigned to the classroom
	 * @throws IllegalArgumentException if the classroomId is null or invalid, or if the list of students is null or empty
	 * @throws ClassroomExistsException if the classroom does not exist
	 */
	@Override
	@Transactional
	public List<Student> assignStudentsToClassroom(Long classroomId, List<Student> students) throws IllegalArgumentException, ClassroomExistsException {
		if (classroomId == null || classroomId <= 0 || students == null || students.isEmpty()) {
			throw new IllegalArgumentException("Invalid classroomId or students");
		}

		Optional<Classroom> classroomOptional = Classroom.findByIdOptional(classroomId);
		if (classroomOptional.isEmpty()) {
			throw new ClassroomExistsException("Classroom does not exist");
		}

		Classroom classroom = classroomOptional.get();
		List<Long> studentIds = students.stream().map(Student::getId).toList();
		Student.update("classroom = ?1 where id in ?2", classroom, studentIds);
		return students;
	}

	/**
	 * Searches for classrooms by name with optional pagination and sorting.
	 *
	 * @param name the name to search for
	 * @param page the pagination object containing pagination parameters (page, size)
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @return a list of Classroom objects
	 */
	@Override
	public List<Classroom> searchByName(String name, Page page, Sort sort) {
		logger.debug("Search Class: " + name);
		return Classroom.find("classroomName LIKE ?1", sort, "%" + name + "%").page(page).list();
	}

	@Override
	@Transactional
	public CodeStatus uploadProfilePicture(Long id, Path imagePath) {
		Optional<Classroom> existingClass = Classroom.findByIdOptional(id);
		if (existingClass.isPresent()) {
			Classroom.update("profilePicture = ?1 where id = ?2", imagePath.toString(), id);
			return CodeStatus.OK;
		}
		return CodeStatus.NOT_FOUND;
	}

	@Override
	@Transactional
	public Optional<Classroom> update(Classroom classroom) throws IllegalArgumentException {
		if (classroom.getId() == null) {
			throw new IllegalArgumentException("Classroom ID is null");
		}

		Optional<Classroom> existingClass = Classroom.findByIdOptional(classroom.getId());
		if (existingClass.isEmpty()) {
			logger.debug("Classroom not found: " + classroom.getId());
			return Optional.empty();
		}

		Classroom existingClassroom = existingClass.get();
		boolean updated = false;
		if (classroom.getClassroomName() != null
			&& !classroom.getClassroomName().equals(existingClassroom.getClassroomName())) {
			logger.debug("Classroom name updated: " + classroom.getClassroomName());
			existingClassroom.setClassroomName(classroom.getClassroomName());
			updated = true;
		}

		if (classroom.getRoom() != null && !classroom.getRoom().equals(existingClassroom.getRoom())) {
			logger.debug("Classroom room updated: " + classroom.getRoom());
			existingClassroom.setRoom(classroom.getRoom());
			updated = true;
		}

		if (classroom.getProfilePicture() != null
			&& !classroom.getProfilePicture().equals(existingClassroom.getProfilePicture())) {
			logger.debug("Classroom profile picture updated: " + classroom.getProfilePicture());
			existingClassroom.setProfilePicture(classroom.getProfilePicture());
			updated = true;
		}

		if (classroom.getTeacher() != null && !classroom.getTeacher().equals(existingClassroom.getTeacher())) {
			logger.debug("Classroom teacher updated: " + classroom.getTeacher());
			existingClassroom.setTeacher(classroom.getTeacher());
			updated = true;
		}

		if (classroom.getGradeLevel() != null && !classroom.getGradeLevel().equals(existingClassroom.getGradeLevel())) {
			logger.debug("Classroom grade level updated: " + classroom.getGradeLevel());
			existingClassroom.setGradeLevel(classroom.getGradeLevel());
			updated = true;
		}

		if (updated) {
			existingClassroom.setUpdatedAt(Instant.now());
			existingClassroom.persistAndFlush();
			Classroom.getEntityManager().refresh(existingClassroom);
			logger.debug("Classroom updated: " + existingClassroom.getId());
			return Optional.of(existingClassroom);
		} else {
			logger.debug("No changes detected for classroom: " + existingClassroom.getId());
		}

		return Optional.empty();
	}
}
