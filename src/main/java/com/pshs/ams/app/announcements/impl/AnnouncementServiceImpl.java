package com.pshs.ams.app.announcements.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

import com.pshs.ams.app.announcements.models.entities.Announcement;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.app.announcements.services.AnnouncementService;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Log4j2
public class AnnouncementServiceImpl implements AnnouncementService {

	/**
	 * Create a new announcement. This method will set the created_at and updated_at
	 * fields before persisting the announcement.
	 *
	 * @param announcement the announcement to be created
	 * @return the status of the operation
	 */
	@Override
	@Transactional
	public CodeStatus createAnnouncement(Announcement announcement) {
		if (announcement.isPersistent()) {
			log.debug("Announcement already exists: {}", announcement.getTitle());
			return CodeStatus.CONFLICT;
		}
		announcement.setCreatedAt(Instant.now());
		announcement.setUpdatedAt(Instant.now());

		announcement.persist();
		return CodeStatus.OK;
	}

	/**
	 * Updates an existing announcement. This method will set the updated_at field
	 * before updating the announcement.
	 *
	 * @param announcement the announcement to be updated
	 * @return the status of the operation
	 */
	@Override
	@Transactional
	public CodeStatus updateAnnouncement(Announcement announcement, Integer id) {
		Optional<Announcement> existingAnnouncement = Announcement.findByIdOptional(id);
		if (existingAnnouncement.isEmpty()) {
			log.debug("Announcement id is not found.");
			return CodeStatus.NOT_FOUND;
		}

		Announcement existing = existingAnnouncement.get();
		existing.setTitle(announcement.getTitle());
		existing.setContent(announcement.getContent());
		existing.setUpdatedAt(Instant.now());
		existing.persist();

		return CodeStatus.OK;
	}

	/**
	 * Delete an existing announcement. This method will also remove the
	 * announcement from all users who have viewed it.
	 *
	 * @param announcement the announcement to be deleted
	 * @return the status of the operation
	 */
	@Override
	@Transactional
	public CodeStatus deleteAnnouncement(Integer id) {
		if (id == null) {
			log.debug("Delete not finished, invalid ID received.");
			return CodeStatus.BAD_REQUEST;
		}

		if (isExist(id)) {
			Announcement.deleteById(id);
			return CodeStatus.OK;
		} else {
			log.debug("Announcement id is not found.");
			return CodeStatus.NOT_FOUND;
		}
	}

	/**
	 * Retrieve an announcement by id.
	 *
	 * @param id the id of the announcement to be retrieved
	 * @return an Optional containing the retrieved announcement if found, otherwise
	 * an empty Optional
	 */
	@Override
	public Optional<Announcement> getAnnouncement(Integer id) {
		if (id == null) {
			log.debug("Announcement id is null.");
			return Optional.empty();
		}

		Optional<Announcement> announcement = Announcement.findByIdOptional(id);
		if (announcement.isEmpty()) {
			log.debug("Announcement id is not found: {}", id);
		}

		return announcement;
	}

	/**
	 * Retrieves all {@link Announcement}s sorted and paged according to the given
	 * {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Announcement}s
	 * @param page the {@link Page} to page the retrieved {@link Announcement}s
	 * @return the list of retrieved {@link Announcement}s
	 */
	@Override
	public List<Announcement> getAllAnnouncements(Sort sort, Page page) {
		if (sort == null) {
			log.debug("Sort is null.");
			return List.of();
		}

		if (page == null) {
			log.debug("Page is null.");
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
			log.debug("Title is null or empty.");
			return List.of();
		}

		if (sort == null) {
			log.debug("Sort is null.");
			return Announcement.find("title LIKE ?1", "%" + title + "%").list();
		}

		if (page == null) {
			log.debug("Page is null.");
			return Announcement.find("title LIKE ?1", sort, "%" + title + "%").list();
		}

		log.debug("Search announcements by title: {}", title);
		return Announcement.find("title LIKE ?1", sort, "%" + title + "%").page(page).list();
	}

	/**
	 * Check if announcement exists by ID
	 *
	 * @param id Announcement ID in the database.
	 * @return true or false
	 */
	@Override
	public boolean isExist(Integer id) {
		return Announcement.count("id = ?1", id) > 0;
	}
}
