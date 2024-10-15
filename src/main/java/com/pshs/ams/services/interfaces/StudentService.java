package com.pshs.ams.services.interfaces;

import com.pshs.ams.models.entities.Student;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

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
	 * Creates a new student.
	 *
	 * @param student the Student to create
	 * @return the created Student
	 */
	CodeStatus createStudent(Student student);

	/**
	 * Deletes the student with the given id.
	 *
	 * @param id the id of the student to delete
	 * @return the status of the delete operation
	 */
	CodeStatus deleteStudent(Long id);

	/**
	 * Retrieves the total number of students.
	 *
	 * @return the total number of students
	 */
	long getTotalStudents();

	/**
	 * Retrieves the total number of students in the given classroom.
	 *
	 * @param classroomId the id of the classroom
	 * @return the total number of students in the given classroom
	 */
	long getTotalStudents(Long classroomId);

	/**
	 * Retrieves the student with the given id.
	 *
	 * @param id the id of the student to retrieve
	 * @return the retrieved Student
	 */
	Optional<Student> getStudent(Long id);

	/**
	 * Searches for students by name.
	 *
	 * @param name the name of the student to search for
	 * @return a list of Student objects that match the search criteria
	 */
	List<Student> searchStudentByName(String name, Sort sort, Page page);

	/**
	 * Assigns a classroom to a student.
	 *
	 * @param id the id of the student to assign the classroom to
	 * @param classroomId the id of the classroom to assign to the student
	 */
	CodeStatus assignClassroomToStudent(Long id, Long classroomId);
}