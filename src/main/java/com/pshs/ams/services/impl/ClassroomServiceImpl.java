package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Classroom;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.ClassroomService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.nio.file.Path;
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
	public CodeStatus uploadClassroomProfilePicture(Long id, Path imagePath) {
		Optional<Classroom> existingClass = Classroom.findByIdOptional(id);
		if (existingClass.isPresent()) {
			existingClass.get().setProfilePicture(imagePath.toString());
			existingClass.get().persist();
			return CodeStatus.OK;
		}
		return CodeStatus.NOT_FOUND;
	}
}