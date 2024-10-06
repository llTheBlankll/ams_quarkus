package com.pshs.ams.services.interfaces;

import com.pshs.ams.models.entities.Classroom;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public interface ClassroomService {

	/**
	 * Retrieves a list of all classes with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Classroom objects
	 */
	List<Classroom> getAllClasses(Sort sort, Page page);

	/**
	 * Creates a new class.
	 *
	 * @param classroom a Student object
	 * @return the created Student object
	 */
	CodeStatus createClass(Classroom classroom);

	/**
	 * Deletes the class with the given id.
	 *
	 * @param id the id of the class to delete
	 */
	CodeStatus deleteClassroom(Integer id);

	/**
	 * Retrieves the class with the given id.
	 *
	 * @param id the id of the class to retrieve
	 * @return the retrieved Classroom object
	 */
	Optional<Classroom> getClassroom(Long id);

	List<Classroom> searchClassroomByName(String name, Page page, Sort sort);
}