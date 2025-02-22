package com.pshs.ams.app.classrooms.impl;

import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.classrooms.services.ClassroomService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
	public List<Classroom> getAllClasses(Sort sort, Page page) {
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
	public CodeStatus createClass(Classroom classroom) {
		if (classroom.getId() != null) {
			logger.debug("Class already exists with ID: " + classroom.getId());
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Classroom> existingClass = Classroom.find("classroomName", classroom.getClassroomName())
				.firstResultOptional();
		if (existingClass.isPresent()) {
			logger.debug("Class already exists: " + existingClass.get().getClassroomName());
			return CodeStatus.EXISTS;
		}

		classroom.persist();
		logger.debug("Class created: " + classroom.getClassroomName());
		return CodeStatus.OK;
	}

	/**
	 * Deletes the class with the given id.
	 *
	 * @param id the id of the class to delete
	 */
	@Override
	@Transactional
	public CodeStatus deleteClassroom(Integer id) {
		logger.debug("Deleting Class: " + id);
		Optional<Classroom> existingClass = Classroom.findByIdOptional(id);
		if (existingClass.isPresent()) {
			logger.debug("Class found: " + existingClass.get().getClassroomName());
			existingClass.get().delete();
			logger.debug("Class deleted: " + existingClass.get().getClassroomName());
			return CodeStatus.OK;
		}

		logger.debug("Class not found: " + id);
		return CodeStatus.NOT_FOUND;
	}

	/**
	 * Retrieves the class with the given id.
	 *
	 * @param id the id of the class to retrieve
	 * @return the retrieved Classroom object
	 */
	@Override
	public Optional<Classroom> getClassroom(Long id) {
		logger.debug("Get Class: " + id);
		return Classroom.findByIdOptional(id);
	}

	/**
	 * @param name
	 * @param page
	 * @param sort
	 * @return
	 */
	@Override
	public List<Classroom> searchClassroomByName(String name, Page page, Sort sort) {
		logger.debug("Search Class: " + name);
		return Classroom.find("classroomName LIKE ?1", sort, "%" + name + "%").page(page).list();
	}

	@Override
	@Transactional
	public CodeStatus uploadClassroomProfilePicture(Long id, Path imagePath) {
		Optional<Classroom> existingClass = Classroom.findByIdOptional(id);
		if (existingClass.isPresent()) {
			Classroom.update("profilePicture = ?1 where id = ?2", imagePath.toString(), id);
			return CodeStatus.OK;
		}
		return CodeStatus.NOT_FOUND;
	}

	@Override
	@Transactional
	public CodeStatus updateClass(Classroom classroom) {
		if (classroom.getId() == null) {
			logger.debug("Classroom ID is null");
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Classroom> existingClass = Classroom.findByIdOptional(classroom.getId());
		if (existingClass.isPresent()) {
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
				existingClassroom.persist();
				logger.debug("Classroom updated: " + existingClassroom.getId());
			} else {
				logger.debug("No changes detected for classroom: " + existingClassroom.getId());
			}
			return CodeStatus.OK;
		}
		return CodeStatus.NOT_FOUND;
	}
}
