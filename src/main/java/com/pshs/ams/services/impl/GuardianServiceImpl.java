package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Guardian;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.GuardianService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

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
		return List.of();
	}

	/**
	 * Retrieves the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to retrieve
	 * @return the retrieved {@link Guardian} or {@code null} if no such {@link Guardian} exists
	 */
	@Override
	public Guardian getGuardianById(Integer id) {
		return null;
	}

	/**
	 * Creates a new {@link Guardian} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to create
	 * @return the created {@link Guardian}
	 */
	@Override
	public Guardian createGuardian(Guardian guardian) {
		return null;
	}

	/**
	 * Updates the {@link Guardian} with the given {@code id} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to update
	 * @return the status of the update operation
	 */
	@Override
	public CodeStatus updateGuardian(Guardian guardian) {
		return null;
	}

	/**
	 * Deletes the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to delete
	 * @return the status of the delete operation
	 */
	@Override
	public CodeStatus deleteGuardian(Integer id) {
		return null;
	}
}