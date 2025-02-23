package com.pshs.ams.app.classrooms.services;

import com.pshs.ams.app.classrooms.exceptions.ClassroomExistsException;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.global.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

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
	 * @param classroom a Classroom object representing the class to create
	 * @return an Optional containing the created Classroom object, or an empty Optional if the classroom already exists
	 * @throws ClassroomExistsException if the classroom already exists
	 * @throws IllegalArgumentException if the given classroom is null
	 */
	Optional<Classroom> createClass(Classroom classroom) throws ClassroomExistsException, IllegalArgumentException;

	/**
	 * Updates the details of an existing classroom.
	 *
	 * @param classroom the Classroom object containing updated information
	 * @return an Optional containing the updated Classroom object, or an empty Optional if the classroom does not exist
	 */
	Optional<Classroom> updateClass(Classroom classroom) throws IllegalArgumentException;

	/**
	 * Deletes the class with the given id.
	 *
	 * @param id the id of the class to delete
	 * @throws ClassroomExistsException if the class does not exist
	 * @throws IllegalArgumentException if the given id is null or invalid
	 */
	void deleteClassroom(Integer id) throws ClassroomExistsException, IllegalArgumentException;

	/**
	 * Assigns a list of students to a specified classroom.
	 *
	 * @param classroomId the id of the classroom to which the students will be assigned
	 * @param students a list of Student objects to be assigned to the classroom
	 * @return a list of Student objects that have been successfully assigned to the classroom
	 * @throws IllegalArgumentException if the classroomId is null or invalid, or if the list of students is null or empty
	 * @throws ClassroomExistsException if the classroom does not exist
	 */
	List<Student> assignStudentsToClassroom(Long classroomId, List<Student> students) throws IllegalArgumentException, ClassroomExistsException;

	/**
	 * Retrieves the class with the given id.
	 *
	 * @param id the id of the class to retrieve
	 * @return the retrieved Classroom object
	 */
	Optional<Classroom> getClassroom(Long id);

	/**
	 * Uploads a profile picture for a classroom.
	 *
	 * @param id        the id of the classroom to upload the profile picture for
	 * @param imagePath the path to the image file to upload
	 * @return the status of the operation
	 */
	CodeStatus uploadClassroomProfilePicture(Long id, Path imagePath);

	/**
	 * Searches for classrooms by name with optional pagination and sorting.
	 *
	 * @param name the name to search for
	 * @param page the pagination object containing pagination parameters (page, size)
	 * @param sort a sorting object containing sorting parameters (sortBy, sortDirection)
	 * @return a list of Classroom objects
	 */
	List<Classroom> searchClassroomByName(String name, Page page, Sort sort);
}
