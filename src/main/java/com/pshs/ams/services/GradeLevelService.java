package com.pshs.ams.services;

import com.pshs.ams.models.entities.GradeLevel;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;

public interface GradeLevelService {
	/**
	 * Retrieves a list of all grade levels with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of GradeLevel objects
	 */
	List<GradeLevel> getAllGradeLevel(Sort sort, Page page);

	/**
	 * Retrieves the grade level with the given id.
	 *
	 * @param id the id of the grade level to retrieve
	 * @return the retrieved GradeLevel object
	 */
	GradeLevel getGradeLevelById(Integer id);

	/**
	 * Creates a new grade level.
	 *
	 * @param gradeLevel a GradeLevel object
	 * @return the CodeStatus of the operation
	 */
	CodeStatus createGradeLevel(GradeLevel gradeLevel);

	/**
	 * Deletes the grade level with the given id.
	 *
	 * @param id the id of the grade level to delete
	 * @return the CodeStatus of the operation
	 */
	CodeStatus deleteGradeLevel(Integer id);

	/**
	 * Retrieves the grade level with the given name.
	 *
	 * @param name the name of the grade level to retrieve
	 * @return the retrieved GradeLevel object
	 */
	GradeLevel getGradeLevelByName(String name);

	/**
	 * Retrieves a list of grade levels whose name contains the given string.
	 *
	 * @param name the string to search for in the grade levels' names
	 * @return a list of GradeLevel objects whose name contains the given string
	 */
	List<GradeLevel> searchGradeLevelByName(String name);
}