package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Teacher;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.TeacherService;
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
public class TeacherServiceImpl implements TeacherService {

	@Inject
	Logger logger;

	/**
	 * Retrieves a list of all {@link Teacher}s sorted and paged according to the
	 * given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Teacher}s
	 * @param page the {@link Page} to page the retrieved {@link Teacher}s
	 * @return a list of retrieved {@link Teacher}s
	 */
	@Override
	public List<Teacher> getAllTeacher(Sort sort, Page page) {
		return Teacher.findAll(sort).page(page).list();
	}

	/**
	 * Retrieves the {@link Teacher} with the given id.
	 *
	 * @param id the id of the {@link Teacher} to retrieve
	 * @return the retrieved {@link Teacher} object
	 */
	@Override
	public Optional<Teacher> getTeacher(Long id) {
		return Teacher.findByIdOptional(id);
	}

	/**
	 * Creates a new {@link Teacher}.
	 *
	 * @param teacher the {@link Teacher} to create
	 * @return the status of the creation
	 */
	@Override
	@Transactional
	public CodeStatus createTeacher(Teacher teacher) {
		logger.debug("Creating Teacher: " + teacher.getLastName());
		if (teacher.getId() != null) {
			logger.debug("Teacher already exists with ID: " + teacher.getId());
			return CodeStatus.BAD_REQUEST;
		}

		if (teacher.isPersistent()) {
			// Already exists
			logger.debug("Teacher already exists: " + teacher.getLastName());
			return CodeStatus.EXISTS;
		}

		teacher.persist();
		return CodeStatus.OK;
	}

	/**
	 * Updates an existing {@link Teacher}.
	 *
	 * @param teacher the {@link Teacher} to update
	 * @return the status of the update operation
	 */
	@Override
	@Transactional
	public CodeStatus updateTeacher(Teacher teacher) {
		if (teacher == null) {
			logger.debug("Teacher is null");
			return CodeStatus.NULL;
		}

		if (teacher.isPersistent()) {
			teacher.persist();
			return CodeStatus.OK;
		}

		logger.debug("Teacher not found: " + teacher.getId());
		return CodeStatus.NOT_FOUND;
	}

	/**
	 * Deletes the {@link Teacher} with the given id.
	 *
	 * @param id the id of the {@link Teacher} to delete
	 * @return the status of the delete operation
	 */
	@Override
	@Transactional
	public CodeStatus deleteTeacher(Integer id) {
		if (id <= 0) {
			logger.debug("Invalid id: " + id);
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Teacher> existingTeacher = Teacher.findByIdOptional(id);
		if (existingTeacher.isPresent()) {
			logger.debug("Teacher found: " + existingTeacher.get().getLastName());
			existingTeacher.get().delete();
			logger.debug("Teacher deleted: " + existingTeacher.get().getLastName());
			return CodeStatus.OK;
		}

		logger.debug("Teacher not found: " + id);
		return CodeStatus.NOT_FOUND;
	}

	@Override
	public List<Teacher> searchTeacherByName(String name, Page page, Sort sort) {
		if (name.isEmpty()) {
			return List.of();
		}

		return Teacher.find("firstName LIKE ?1 OR lastName LIKE ?2", sort, "%" + name + "%", "%" + name + "%").page(page)
				.list();
	}

	@Override
	@Transactional
	public CodeStatus uploadTeacherProfilePicture(Long id, Path imagePath) {
		Optional<Teacher> existingTeacher = Teacher.findByIdOptional(id);
		if (existingTeacher.isPresent()) {
			existingTeacher.get().setProfilePicture(imagePath.toString());
			existingTeacher.get().persist();
			return CodeStatus.OK;
		}

		return CodeStatus.NOT_FOUND;
	}
}