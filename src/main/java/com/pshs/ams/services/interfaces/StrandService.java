package com.pshs.ams.services.interfaces;


import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public interface StrandService {

	/**
	 * Retrieves a list of Strand entities based on the specified sort and page configurations.
	 *
	 * @param sort the sort configuration for the list
	 * @param page the page configuration for the list
	 * @return a list of Strand entities
	 */
	List<Strand> getAllStrand(Sort sort, Page page);

	/**
	 * Creates a new Strand entity in the system.
	 *
	 * @param strand the Strand entity to be created
	 * @return the status of the creation operation
	 */
	Optional<Strand> createStrand(Strand strand);

	Optional<Strand> getStrand(Integer id);

	CodeStatus deleteStrand(Integer id);

	CodeStatus updateStrand(Strand strand, Integer id);
}
