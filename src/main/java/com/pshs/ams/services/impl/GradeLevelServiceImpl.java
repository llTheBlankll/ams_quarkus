package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.GradeLevel;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.GradeLevelService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GradeLevelServiceImpl implements GradeLevelService {
	/**
	 * Retrieves a list of all grade levels with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of GradeLevel objects
	 */
	@Override
	public List<GradeLevel> getAllGradeLevel(Sort sort, Page page) {
		return GradeLevel.findAll(sort).page(page).list();
	}

	/**
	 * Retrieves the grade level with the given id.
	 *
	 * @param id the id of the grade level to retrieve
	 * @return the retrieved GradeLevel object
	 */
	@Override
	public GradeLevel getGradeLevelById(Integer id) {
		return GradeLevel.findById(id);
	}

	/**
	 * Creates a new grade level.
	 *
	 * @param gradeLevel a GradeLevel object
	 * @return the CodeStatus of the operation
	 */
	@Override
	public CodeStatus createGradeLevel(GradeLevel gradeLevel) {
		// When creating grade level, the id should be null
		if (gradeLevel.getId() != null) {
			return CodeStatus.BAD_REQUEST;
		}

		// Check if the grade level already exists
		Optional<GradeLevel> existingGradeLevel = GradeLevel.find("name", gradeLevel.getName()).firstResultOptional();
		if (existingGradeLevel.isPresent()) {
			return CodeStatus.EXISTS;
		}

		gradeLevel.persist();
		return CodeStatus.OK;
	}

	/**
	 * Deletes the grade level with the given id.
	 *
	 * @param id the id of the grade level to delete
	 * @return the CodeStatus of the operation
	 */
	@Override
	public CodeStatus deleteGradeLevel(Integer id) {
		// Check if exists
		Optional<GradeLevel> existingGradeLevel = GradeLevel.findByIdOptional(id);
		if (existingGradeLevel.isEmpty()) {
			return CodeStatus.NOT_FOUND;
		}

		existingGradeLevel.get().delete();
		return CodeStatus.OK;
	}

	/**
	 * Retrieves the grade level with the given name.
	 *
	 * @param name the name of the grade level to retrieve
	 * @return the retrieved GradeLevel object
	 */
	@Override
	public Optional<GradeLevel> getGradeLevelByName(String name) {
		return GradeLevel.find("name", name).firstResultOptional();
	}

	/**
	 * Retrieves a list of grade levels whose name contains the given string.
	 *
	 * @param name the string to search for in the grade levels' names
	 * @return a list of GradeLevel objects whose name contains the given string
	 */
	@Override
	public List<GradeLevel> searchGradeLevelByName(String name) {
		if (name.isEmpty()) {
			return List.of();
		}

		return GradeLevel.find("name like ?1", "%" + name + "%").list();
	}
}