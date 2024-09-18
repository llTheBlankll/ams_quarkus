package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Student;
import com.pshs.ams.services.StudentService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StudentServiceImpl implements StudentService {
	/**
	 * Retrieves a list of all students with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Student objects
	 */
	@Override
	public List<Student> getAllStudents(Sort sort, Page page) {
		return List.of();
	}

	/**
	 * Retrieves a list of all classes with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Classroom objects
	 */
	@Override
	public List<Student> getAllClasses(Sort sort, Page page) {
		return List.of();
	}

	/**
	 * Creates a new class.
	 *
	 * @param student a Student object
	 * @return the created Student object
	 */
	@Override
	public Student createClass(Student student) {
		return null;
	}

	/**
	 * Deletes the class with the given id.
	 *
	 * @param id the id of the class to delete
	 */
	@Override
	public void deleteClass(Long id) {

	}

	/**
	 * Retrieves the class with the given id.
	 *
	 * @param id the id of the class to retrieve
	 * @return the retrieved Classroom object
	 */
	@Override
	public Student getClassroom(Long id) {
		return null;
	}
}