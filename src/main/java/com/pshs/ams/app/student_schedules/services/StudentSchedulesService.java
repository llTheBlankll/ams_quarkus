package com.pshs.ams.app.student_schedules.services;

import com.pshs.ams.app.student_schedules.models.entities.StudentSchedule;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public interface StudentSchedulesService {

	/**
	 * Create a new student schedule.
	 *
	 * @param studentSchedule the student schedule to be created
	 * @return the newly created student schedule, if successful
	 */
	Optional<StudentSchedule> create(StudentSchedule studentSchedule);


	/**
	 * Updates an existing student schedule.
	 *
	 * @param studentSchedule the student schedule containing the updated information
	 * @param id              the id of the student schedule to update
	 * @return an Optional containing the updated student schedule if successful,
	 * or an empty Optional if the update was not successful
	 */
	Optional<StudentSchedule> update(StudentSchedule studentSchedule, Integer id);

	/**
	 * Deletes a student schedule by its id.
	 *
	 * @param id the id of the student schedule to delete
	 * @return an Optional containing the deleted student schedule if successful,
	 * or an empty Optional if the delete was not successful
	 */
	Optional<StudentSchedule> delete(Integer id);

	/**
	 * Retrieves a student schedule by its id.
	 *
	 * @param id the id of the student schedule to retrieve
	 * @return an Optional containing the student schedule if found, or an empty
	 * Optional if no student schedule was found with the given id
	 */
	Optional<StudentSchedule> get(Integer id);

	/**
	 * Retrieves a list of all student schedules.
	 *
	 * @return a list of all student schedules
	 */
	List<StudentSchedule> listAll(Page page, Sort sort);
}
