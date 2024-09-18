package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.GradeLevel;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.GradeLevelService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

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
		return List.of();
	}

	/**
	 * Retrieves the grade level with the given id.
	 *
	 * @param id the id of the grade level to retrieve
	 * @return the retrieved GradeLevel object
	 */
	@Override
	public GradeLevel getGradeLevelById(Integer id) {
		return null;
	}

	/**
	 * Creates a new grade level.
	 *
	 * @param gradeLevel a GradeLevel object
	 * @return the CodeStatus of the operation
	 */
	@Override
	public CodeStatus createGradeLevel(GradeLevel gradeLevel) {
		return null;
	}

	/**
	 * Deletes the grade level with the given id.
	 *
	 * @param id the id of the grade level to delete
	 * @return the CodeStatus of the operation
	 */
	@Override
	public CodeStatus deleteGradeLevel(Integer id) {
		return null;
	}

	/**
	 * Retrieves the grade level with the given name.
	 *
	 * @param name the name of the grade level to retrieve
	 * @return the retrieved GradeLevel object
	 */
	@Override
	public GradeLevel getGradeLevelByName(String name) {
		return null;
	}

	/**
	 * Retrieves a list of grade levels whose name contains the given string.
	 *
	 * @param name the string to search for in the grade levels' names
	 * @return a list of GradeLevel objects whose name contains the given string
	 */
	@Override
	public List<GradeLevel> searchGradeLevelByName(String name) {
		return List.of();
	}
}