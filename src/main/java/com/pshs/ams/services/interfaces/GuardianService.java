package com.pshs.ams.services.interfaces;

import com.pshs.ams.models.entities.Guardian;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public interface GuardianService {

	/**
	 * Retrieves all {@link Guardian}s sorted and paged according to the given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Guardian}s
	 * @param page the {@link Page} to page the retrieved {@link Guardian}s
	 * @return the list of retrieved {@link Guardian}s
	 */
	List<Guardian> getAllGuardian(Sort sort, Page page);

	/**
	 * Retrieves the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to retrieve
	 * @return the retrieved {@link Guardian} or {@code null} if no such {@link Guardian} exists
	 */
	Optional<Guardian> getGuardianById(Integer id);


	/**
	 * Creates a new {@link Guardian} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to create
	 * @return the created {@link Guardian}
	 */
	CodeStatus createGuardian(Guardian guardian);

	/**
	 * Updates the {@link Guardian} with the given {@code id} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to update
	 * @return the status of the update operation
	 */
	CodeStatus updateGuardian(Guardian guardian);

	/**
	 * Deletes the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to delete
	 * @return the status of the delete operation
	 */
	CodeStatus deleteGuardian(Integer id);
}