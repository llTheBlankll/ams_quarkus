package com.pshs.ams.app.guardians.impl;

import com.pshs.ams.app.guardians.models.entities.Guardian;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.guardians.services.GuardianService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GuardianServiceImpl implements GuardianService {
	/**
	 * Retrieves all {@link Guardian}s sorted and paged according to the given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Guardian}s
	 * @param page the {@link Page} to page the retrieved {@link Guardian}s
	 * @return the list of retrieved {@link Guardian}s
	 */
	@Override
	public List<Guardian> getAllGuardian(Sort sort, Page page) {
		return Guardian.findAll(sort).page(page).list();
	}

	/**
	 * Retrieves the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to retrieve
	 * @return the retrieved {@link Guardian} or {@code null} if no such {@link Guardian} exists
	 */
	@Override
	public Optional<Guardian> getGuardianById(Integer id) {
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
	public CodeStatus createGuardian(Guardian guardian) {
		// The id should be null
		if (guardian.getId() != null) {
			return CodeStatus.BAD_REQUEST;
		}

		// Check if exists
		if (Guardian.find("fullName", guardian.getFullName()).count() > 0) {
			return CodeStatus.EXISTS;
		}

		guardian.persist();
		return CodeStatus.OK;
	}

	/**
	 * Updates the {@link Guardian} with the given {@code id} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to update
	 * @return the status of the update operation
	 */
	@Override
	@Transactional
	public CodeStatus updateGuardian(Guardian guardian, Integer id) {
		// The id should not be null
		if (guardian.getId() == null || id <= 0) {
			return CodeStatus.BAD_REQUEST;
		}

		// Check if exists
		Optional<Guardian> existingGuardian = Guardian.findByIdOptional(id);
		if (existingGuardian.isEmpty()) {
			return CodeStatus.NOT_FOUND;
		}

		if (guardian.isPersistent()) {
			guardian.persist();
			return CodeStatus.OK;
		}

		return CodeStatus.NOT_FOUND;
	}

	/**
	 * Deletes the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to delete
	 * @return the status of the delete operation
	 */
	@Override
	@Transactional
	public CodeStatus deleteGuardian(Integer id) {
		Optional<Guardian> existingGuardian = Guardian.findByIdOptional(id);
		if (existingGuardian.isPresent()) {
			existingGuardian.get().delete();
			return CodeStatus.OK;
		}

		return CodeStatus.NOT_FOUND;
	}
}
