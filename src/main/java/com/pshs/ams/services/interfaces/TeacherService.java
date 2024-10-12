package com.pshs.ams.services.interfaces;

import com.pshs.ams.models.entities.Teacher;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface TeacherService {

	/**
	 * Retrieves a list of all {@link Teacher}s sorted and paged according to the
	 * given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Teacher}s
	 * @param page the {@link Page} to page the retrieved {@link Teacher}s
	 * @return a list of retrieved {@link Teacher}s
	 */
	List<Teacher> getAllTeacher(Sort sort, Page page);

	/**
	 * Retrieves the {@link Teacher} with the given id.
	 *
	 * @param id the id of the {@link Teacher} to retrieve
	 * @return the retrieved {@link Teacher} object
	 */
	@Transactional
	Optional<Teacher> getTeacher(Long id);

	/**
	 * Creates a new {@link Teacher}.
	 *
	 * @param teacher the {@link Teacher} to create
	 * @return the status of the creation
	 */
	CodeStatus createTeacher(Teacher teacher);

	/**
	 * Updates an existing {@link Teacher}.
	 *
	 * @param teacher the {@link Teacher} to update
	 * @return the status of the update operation
	 */
	CodeStatus updateTeacher(Teacher teacher);

	/**
	 * Uploads a profile picture for a teacher.
	 *
	 * @param id        the id of the teacher to upload the profile picture for
	 * @param imagePath the path to the image file to upload
	 * @return the status of the operation
	 */
	CodeStatus uploadTeacherProfilePicture(Long id, Path imagePath);

	/**
	 * Deletes the {@link Teacher} with the given id.
	 *
	 * @param id the id of the {@link Teacher} to delete
	 * @return the status of the delete operation
	 */
	CodeStatus deleteTeacher(Integer id);

	List<Teacher> searchTeacherByName(String name, Page page, Sort sort);
}
