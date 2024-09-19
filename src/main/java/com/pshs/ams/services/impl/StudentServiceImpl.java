package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Student;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.StudentService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StudentServiceImpl implements StudentService {

	@Inject
	Logger logger;

	/**
	 * Retrieves a list of all students with optional sorting and pagination.
	 *
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @param page a pagination object containing pagination parameters (page, size)
	 * @return a list of Student objects
	 */
	@Override
	public List<Student> getAllStudents(Sort sort, Page page) {
		logger.debug("Get all students: " + sort + ", " + page);
		return Student.findAll(sort).page(page).list();
	}

	/**
	 * Creates a new student.
	 *
	 * @param student the Student to create
	 * @return the created Student
	 */
	@Override
	public CodeStatus createStudent(Student student) {
		if (student == null) {
			logger.debug("Student is null");
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Student> existingStudent = Student.find("lrn", student.getId()).firstResultOptional();
		if (existingStudent.isPresent()) {
			logger.debug("Student already exists");
			return CodeStatus.EXISTS;
		}

		student.persist();
		logger.debug("Student created: " + student);
		return CodeStatus.OK;
	}

	/**
	 * Deletes the student with the given id.
	 *
	 * @param id the id of the student to delete
	 * @return the status of the delete operation
	 */
	@Override
	public CodeStatus deleteStudent(Long id) {
		if (id <= 0) {
			logger.debug("Invalid id: " + id);
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Student> existingStudent = Student.findByIdOptional(id);
		if (existingStudent.isPresent()) {
			existingStudent.get().delete();
			logger.debug("Student deleted: " + id);
			return CodeStatus.OK;
		}

		logger.debug("Student not found: " + id);
		return CodeStatus.NOT_FOUND;
	}

	/**
	 * Retrieves the total number of students.
	 *
	 * @return the total number of students
	 */
	@Override
	public long getTotalStudents() {
		return Student.count();
	}

	/**
	 * Retrieves the student with the given id.
	 *
	 * @param id the id of the student to retrieve
	 * @return the retrieved Student
	 */
	@Override
	public Optional<Student> getStudent(Long id) {
		if (id <= 0) {
			logger.debug("Invalid id: " + id);
			return Optional.empty();
		}

		return Student.findByIdOptional(id);
	}
}