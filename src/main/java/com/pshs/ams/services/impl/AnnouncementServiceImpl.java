package com.pshs.ams.services.impl;

import com.pshs.ams.models.entities.Announcement;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.services.interfaces.AnnouncementService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AnnouncementServiceImpl implements AnnouncementService {

	@Inject
	Logger logger;

	/**
	 * Create a new announcement. This method will set the created_at and updated_at fields before persisting the announcement.
	 *
	 * @param announcement the announcement to be created
	 * @return the status of the operation
	 */
	@Override
	public CodeStatus createAnnouncement(Announcement announcement) {
		if (announcement.isPersistent()) {
			logger.debug("Announcement already exists: {}", announcement.getTitle());
			return CodeStatus.EXISTS;
		}

		announcement.persist();
		return CodeStatus.OK;
	}

	/**
	 * Updates an existing announcement. This method will set the updated_at field before updating the announcement.
	 *
	 * @param announcement the announcement to be updated
	 * @return the status of the operation
	 */
	@Override
	public CodeStatus updateAnnouncement(Announcement announcement, Long id) {
		Optional<Announcement> existingAnnouncement = Announcement.findByIdOptional(id);
		if (existingAnnouncement.isEmpty()) {
			logger.debug("Announcement id is not found.");
			return CodeStatus.NOT_FOUND;
		}

		Announcement existing = existingAnnouncement.get();
		existing.setTitle(announcement.getTitle());
		existing.setContent(announcement.getContent());
		existing.setUpdatedAt(announcement.getUpdatedAt());
		existing.persist();

		return CodeStatus.OK;
	}

	/**
	 * Delete an existing announcement. This method will also remove the announcement from all users who have viewed it.
	 *
	 * @param announcement the announcement to be deleted
	 * @return the status of the operation
	 */
	@Override
	public CodeStatus deleteAnnouncement(Announcement announcement) {
		if (announcement.isPersistent()) {
			announcement.delete();
			return CodeStatus.OK;
		} else {
			logger.debug("Announcement id is not found.");
			return CodeStatus.NOT_FOUND;
		}
	}

	/**
	 * Retrieve an announcement by id.
	 *
	 * @param id the id of the announcement to be retrieved
	 * @return an Optional containing the retrieved announcement if found, otherwise an empty Optional
	 */
	@Override
	public Optional<Announcement> getAnnouncement(Long id) {
		if (id == null) {
			logger.debug("Announcement id is null.");
			return Optional.empty();
		}

		Optional<Announcement> announcement = Announcement.findByIdOptional(id);
		if (announcement.isEmpty()) {
			logger.debug("Announcement id is not found: {}", id);
		}

		return announcement;
	}

	/**
	 * Retrieves all {@link Announcement}s sorted and paged according to the given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Announcement}s
	 * @param page the {@link Page} to page the retrieved {@link Announcement}s
	 * @return the list of retrieved {@link Announcement}s
	 */
	@Override
	public List<Announcement> getAllAnnouncements(Sort sort, Page page) {
		if (sort == null) {
			logger.debug("Sort is null.");
			return List.of();
		}

		if (page == null) {
			logger.debug("Page is null.");
			return Announcement.listAll(sort);
		}

		return Announcement.findAll(sort).page(page).list();
	}

	/**
	 * Searches for announcements by title with optional pagination and sorting.
	 *
	 * @param title the title to search for
	 * @param sort  the {@link Sort} to sort the retrieved {@link Announcement}s
	 * @param page  the {@link Page} to page the retrieved {@link Announcement}s
	 * @return a list of {@link Announcement}s that match the search criteria
	 */
	@Override
	public List<Announcement> searchAnnouncement(String title, Sort sort, Page page) {
		if (title == null || title.isEmpty()) {
			logger.debug("Title is null or empty.");
			return List.of();
		}

		if (sort == null) {
			logger.debug("Sort is null.");
			return Announcement.find("title LIKE ?1", "%" + title + "%").list();
		}

		if (page == null) {
			logger.debug("Page is null.");
			return Announcement.find("title LIKE ?1", sort, "%" + title + "%").list();
		}

		logger.debug("Search announcements by title: {}", title);
		return Announcement.find("title LIKE ?1", sort, "%" + title + "%").page(page).list();
	}

	/**
	 * Check if announcement exists by ID
	 *
	 * @param id Announcement ID in the database.
	 * @return true or false
	 */
	@Override
	public boolean isExist(Long id) {
		return Announcement.count("id = ?1", id) > 0;
	}
}
