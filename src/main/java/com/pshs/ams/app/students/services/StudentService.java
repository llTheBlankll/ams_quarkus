package com.pshs.ams.app.students.services;

import com.pshs.ams.app.students.exceptions.StudentExistsException;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.global.models.custom.LineChart;
import com.pshs.ams.app.strands.models.dto.MostPopularStrandDTO;

import com.pshs.ams.global.models.enums.Sex;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.nio.file.Path;
import java.time.LocalDate;
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
	 * @throws IllegalArgumentException if the given student is null
	 * @throws StudentExistsException   if a student with the same id already exists
	 */
	Optional<Student> createStudent(Student student) throws IllegalArgumentException, StudentExistsException;

	/**
	 * Deletes the student with the given id.
	 *
	 * @param id the id of the student to delete
	 * @throws IllegalArgumentException if the given id is null or invalid
	 * @throws StudentExistsException   if the student does not exist
	 */
	void deleteStudent(Long id) throws IllegalArgumentException, StudentExistsException;

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
	Optional<Student> getStudent(Long id) throws IllegalArgumentException;

	/**
	 * Searches for students by name.
	 *
	 * @param name the name of the student to search for
	 * @return a list of Student objects that match the search criteria
	 */
	List<Student> searchStudentByName(String name, Sort sort, Page page) throws IllegalArgumentException;

	/**
	 * Assigns the student with the given id to the given classroom.
	 *
	 * @param id          the id of the student to assign
	 * @param classroomId the id of the classroom to assign the student to
	 * @return the status of the assignment operation
	 * @throws IllegalArgumentException if the given id or classroomId is null or invalid
	 * @throws StudentExistsException   if the student does not exist
	 */
	CodeStatus assignStudentToClassroom(Long id, Long classroomId) throws IllegalArgumentException, StudentExistsException;

	/**
	 * Uploads a profile picture for the student with the given id.
	 *
	 * @param id the id of the student to upload the profile picture for
	 * @param path the path to the profile picture
	 * @return the status of the upload operation
	 */
	CodeStatus uploadStudentProfilePicture(Long id, Path path);

	long getStudentCountByStrand(Long strandId);

	long getStudentCountByGradeLevel(Long gradeLevelId);

	/**
	 * Counts the number of students by sex.
	 *
	 * @param sex the sex of the students to count
	 * @return the number of students with the given sex
	 */
	long countBySex(Sex sex);

	double getAverageStudentsPerStrand();

	Optional<MostPopularStrandDTO> getMostPopularStrand();

	LineChart getStrandDistribution(LocalDate startDate, LocalDate endDate);
}
