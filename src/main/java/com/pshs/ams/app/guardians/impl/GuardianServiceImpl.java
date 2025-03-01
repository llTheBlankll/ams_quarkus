package com.pshs.ams.app.guardians.impl;

import com.pshs.ams.app.guardians.exceptions.GuardianExistsException;
import com.pshs.ams.app.guardians.exceptions.GuardianNotFoundException;
import com.pshs.ams.app.guardians.models.entities.Guardian;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.guardians.services.GuardianService;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GuardianServiceImpl implements GuardianService {

	@Inject
	Logger logger;

	/**
	 * Retrieves all {@link Guardian}s sorted and paged according to the given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Guardian}s
	 * @param page the {@link Page} to page the retrieved {@link Guardian}s
	 * @return the list of retrieved {@link Guardian}s
	 */
	@Override
	public List<Guardian> listAll(Sort sort, Page page) {
		return Guardian.findAll(sort).page(page).list();
	}

	@Override
	public Optional<Guardian> get(Integer id) throws IllegalArgumentException {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Invalid id: " + id);
		}
		return Guardian.findByIdOptional(id);
	}

	/**
	 * Creates a new {@link Guardian} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to create
	 * @return the created {@link Guardian}
	 */
	@Override
	@Transactional
	public Optional<Guardian> create(Guardian guardian) throws IllegalArgumentException, GuardianExistsException {
		// The id should be null
		if (guardian.getId() != null) {
			throw new IllegalArgumentException("create() - Id should be null because we are saving a new guardian for a student.");
		}

		// Check if exists
		if (Guardian.find("fullName", guardian.getFullName()).count() > 0) {
			logger.debug("Guardian with name " + guardian.getFullName() + " already exists");
			return Optional.empty();
		}

		guardian.persistAndFlush();
		Guardian.getEntityManager().refresh(guardian);
		return Optional.of(guardian);
	}

	/**
	 * Updates the {@link Guardian} with the given {@code id} based on the given {@link Guardian} entity.
	 *
	 * @param newGuardian the {@link Guardian} entity to update
	 * @return the status of the update operation
	 */
	@Override
	@Transactional
	public Optional<Guardian> update(Guardian newGuardian, Integer id) throws IllegalArgumentException, GuardianNotFoundException {
		// The id should not be null
		if (newGuardian.getId() == null || id <= 0) {
			throw new IllegalArgumentException("Invalid id: " + id);
		}

		// Check if exists
		Optional<Guardian> existingGuardian = Guardian.findByIdOptional(id);
		if (existingGuardian.isEmpty()) {
			return Optional.empty();
		}

		Guardian currentGuardian = existingGuardian.get();
		currentGuardian.setFullName(newGuardian.getFullName());
		currentGuardian.setContactNumber(newGuardian.getContactNumber());
		currentGuardian.setStudents(newGuardian.getStudents());
		currentGuardian.persist();
		return Optional.of(newGuardian);
	}

	/**
	 * Deletes the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to delete
	 */
	@Override
	@Transactional
	public void delete(Integer id) {
		Optional<Guardian> existingGuardian = Guardian.findByIdOptional(id);
		existingGuardian.ifPresent(PanacheEntityBase::delete);
	}
}
