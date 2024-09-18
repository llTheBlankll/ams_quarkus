package com.pshs.ams.services;

import com.pshs.ams.models.entities.Student;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;

public interface StudentService {

	/**
	 * Retrieves a list of all students with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Student objects
	 */
	List<Student> getAllStudents(Sort sort, Page page);


	/**
	 * Retrieves a list of all classes with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Classroom objects
	 */
	List<Student> getAllClasses(Sort sort, Page page);

	/**
	 * Creates a new class.
	 *
	 * @param student a Student object
	 * @return the created Student object
	 */
	Student createClass(Student student);

	/**
	 * Deletes the class with the given id.
	 *
	 * @param id the id of the class to delete
	 */
	void deleteClass(Long id);

	/**
	 * Retrieves the class with the given id.
	 *
	 * @param id the id of the class to retrieve
	 * @return the retrieved Classroom object
	 */
	Student getClassroom(Long id);
}