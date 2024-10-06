package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Strand;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.StrandService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StrandServiceImpl implements StrandService {

	@Inject
	Logger logger;

	/**
	 * Retrieves all Strand entities.
	 *
	 * @param sort the sort configuration
	 * @param page the page configuration
	 * @return the list of Strand entities
	 */
	@Override
	public List<Strand> getAllStrand(Sort sort, Page page) {
		logger.debug("Get all Strand");
		return Strand.findAll(sort).page(page).list();
	}

	/**
	 * Creates a new Strand entity.
	 *
	 * @param strand the Strand entity to be created
	 * @return the created Strand entity
	 */
	@Override
	@Transactional
	public CodeStatus createStrand(Strand strand) {
		if (strand == null) {
			logger.debug("Strand is null");
			return CodeStatus.NULL;
		}

		if (strand.getId() != null) {
			logger.debug("Strand with id " + strand.getId() + " already exists");
			return CodeStatus.EXISTS;
		}

		if (Strand.find("name", strand.getName()).count() > 0) {
			logger.debug("Strand with name " + strand.getName() + " already exists");
			return CodeStatus.EXISTS;
		}

		logger.debug("Create Strand: " + strand);
		strand.persist();
		return CodeStatus.OK;
	}

	/**
	 * Retrieves a Strand entity by its primary key id.
	 *
	 * @param id the primary key of the Strand entity
	 * @return the Strand entity if found, null otherwise
	 */
	@Override
	public Optional<Strand> getStrand(Integer id) {
		return Strand.findByIdOptional(id);
	}

	/**
	 * Deletes a Strand entity by its primary key id.
	 *
	 * @param id the primary key of the Strand entity
	 * @return the status of the deletion
	 */
	@Override
	@Transactional
	public CodeStatus deleteStrand(Integer id) {
		if (id <= 0) {
			logger.debug("Invalid Strand id: " + id);
			return CodeStatus.BAD_REQUEST;
		}

		Optional<Strand> strand = getStrand(id);
		if (strand.isEmpty()) {
			logger.debug("Strand with id " + id + " not found");
			return CodeStatus.NOT_FOUND;
		}

		logger.debug("Delete Strand: " + strand.get());
		strand.get().delete();
		return CodeStatus.OK;
	}

	/**
	 * Updates a Strand entity.
	 *
	 * @param strand the Strand entity to be updated
	 * @return the status of the update
	 */
	@Override
	@Transactional
	public CodeStatus updateStrand(Strand strand, Integer id) {
		if (strand == null || id <= 0) {
			logger.debug("Strand is null");
			return CodeStatus.NULL;
		}

		Optional<Strand> existingStrand = Strand.findByIdOptional(id);
		if (existingStrand.isEmpty()) {
			logger.debug("Strand with id " + strand.getId() + " not found");
			return CodeStatus.NOT_FOUND;
		}

		logger.debug("Update Strand: " + strand);
		strand.setId(id);
		strand.persist();
		return CodeStatus.OK;
	}
}