package com.pshs.ams.app.guardians.services;

import com.pshs.ams.app.guardians.exceptions.GuardianExistsException;
import com.pshs.ams.app.guardians.exceptions.GuardianNotFoundException;
import com.pshs.ams.app.guardians.models.entities.Guardian;
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
	List<Guardian> listAll(Sort sort, Page page);

	/**
	 * Retrieves the {@link Guardian} with the given {@code id}.
	 *
	 * @param id the id of the {@link Guardian} to retrieve
	 * @return the retrieved {@link Guardian} or {@code null} if no such {@link Guardian} exists
	 */
	Optional<Guardian> get(Integer id) throws IllegalArgumentException;

	/**
	 * Creates a new {@link Guardian} based on the given {@link Guardian} entity.
	 *
	 * @param guardian the {@link Guardian} entity to create
	 * @return the created {@link Guardian}
	 */
	Optional<Guardian> create(Guardian guardian) throws IllegalArgumentException;

	Optional<Guardian> update(Guardian guardian, Integer id) throws GuardianNotFoundException, IllegalArgumentException;

	void delete(Integer id) throws IllegalArgumentException, GuardianNotFoundException;
}
