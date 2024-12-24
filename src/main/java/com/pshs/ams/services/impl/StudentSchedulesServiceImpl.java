package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.StudentSchedule;
import com.pshs.ams.services.interfaces.StudentSchedulesService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StudentSchedulesServiceImpl implements StudentSchedulesService {

	Logger logger = LogManager.getLogger(this.getClass());

	/**
	 * Create a new student schedule.
	 *
	 * @param studentSchedule the student schedule to be created
	 * @return the newly created student schedule, if successful
	 */
	@Override
	public Optional<StudentSchedule> create(StudentSchedule studentSchedule) {
		if (studentSchedule == null) {
			return Optional.empty();
		}

		if (studentSchedule.getOnTime() == null || studentSchedule.getAbsentTime() == null || studentSchedule.getLateTime() == null) {
			return Optional.empty();
		}

		studentSchedule.persist();
		return Optional.of(studentSchedule);
	}

	/**
	 * Updates an existing student schedule.
	 *
	 * @param studentSchedule the student schedule containing the updated information
	 * @param id              the id of the student schedule to update
	 * @return an Optional containing the updated student schedule if successful,
	 * or an empty Optional if the update was not successful
	 */
	@Override
	public Optional<StudentSchedule> update(StudentSchedule studentSchedule, Integer id) {
		Optional<StudentSchedule> schedule = get(id);

		if (schedule.isPresent()) {
			logger.debug("Updating Schedule with ID {}", id);
			StudentSchedule currentSchedule = schedule.get();
			currentSchedule.setAbsentTime(studentSchedule.getAbsentTime());
			currentSchedule.setOnTime(studentSchedule.getOnTime());
			currentSchedule.setIsFlag(studentSchedule.getIsFlag());
			currentSchedule.setLateTime(studentSchedule.getLateTime());
			currentSchedule.setId(id);
			currentSchedule.persist();
			logger.debug("Schedule with ID {} updated", id);
			return schedule;
		}

		logger.debug("Schedule with ID {} not found", id);
		return Optional.empty();
	}

	/**
	 * Deletes a student schedule by its id.
	 *
	 * @param id the id of the student schedule to delete
	 * @return an Optional containing the deleted student schedule if successful,
	 * or an empty Optional if the delete was not successful
	 */
	@Override
	public Optional<StudentSchedule> delete(Integer id) {
		Optional<StudentSchedule> schedule = get(id);

		if (schedule.isPresent()) {
			logger.debug("Deleting Schedule with ID {}", id);
			schedule.get().delete();
			logger.debug("Schedule with ID {} deleted", id);
			return schedule;
		}

		logger.debug("Schedule with ID {} not found", id);
		return Optional.empty();
	}

	/**
	 * Retrieves a student schedule by its id.
	 *
	 * @param id the id of the student schedule to retrieve
	 * @return an Optional containing the student schedule if found, or an empty
	 * Optional if no student schedule was found with the given id
	 */
	@Override
	public Optional<StudentSchedule> get(Integer id) {
		return StudentSchedule.findByIdOptional(id);
	}

	/**
	 * Retrieves a list of all student schedules.
	 *
	 * @return a list of all student schedules
	 */
	@Override
	public List<StudentSchedule> listAll(Page page, Sort sort) {
		return StudentSchedule.findAll(sort).page(page).list();
	}
}
