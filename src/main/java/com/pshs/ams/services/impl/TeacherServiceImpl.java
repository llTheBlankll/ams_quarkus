package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Teacher;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.TeacherService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TeacherServiceImpl implements TeacherService {
	/**
	 * Retrieves a list of all {@link Teacher}s sorted and paged according to the given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Teacher}s
	 * @param page the {@link Page} to page the retrieved {@link Teacher}s
	 * @return a list of retrieved {@link Teacher}s
	 */
	@Override
	public List<Teacher> getAllTeacher(Sort sort, Page page) {
		return List.of();
	}

	/**
	 * Retrieves the {@link Teacher} with the given id.
	 *
	 * @param id the id of the {@link Teacher} to retrieve
	 * @return the retrieved {@link Teacher} object
	 */
	@Override
	public Teacher getTeacher(Long id) {
		return null;
	}

	/**
	 * Creates a new {@link Teacher}.
	 *
	 * @param teacher the {@link Teacher} to create
	 * @return the status of the creation
	 */
	@Override
	public CodeStatus createTeacher(Teacher teacher) {
		return null;
	}

	/**
	 * Updates an existing {@link Teacher}.
	 *
	 * @param teacher the {@link Teacher} to update
	 * @return the status of the update operation
	 */
	@Override
	public CodeStatus updateTeacher(Teacher teacher) {
		return null;
	}

	/**
	 * Deletes the {@link Teacher} with the given id.
	 *
	 * @param id the id of the {@link Teacher} to delete
	 * @return the status of the delete operation
	 */
	@Override
	public CodeStatus deleteTeacher(Integer id) {
		return null;
	}

	/**
	 * Retrieves a list of {@link Teacher}s whose name contains the given string.
	 *
	 * @param name the string to search for in the teachers' names
	 * @return a list of {@link Teacher}s whose name contains the given string
	 */
	@Override
	public List<Teacher> searchTeacherByName(String name) {
		return List.of();
	}
}