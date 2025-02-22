package com.pshs.ams.app.announcements.services;

import com.pshs.ams.app.announcements.models.entities.Announcement;
import com.pshs.ams.global.models.enums.CodeStatus;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public interface AnnouncementService {

	/**
	 * Create a new announcement. This method will set the created_at and updated_at fields before persisting the announcement.
	 *
	 * @param announcement the announcement to be created
	 * @return the status of the operation
	 */
	CodeStatus createAnnouncement(Announcement announcement);

	/**
	 * Updates an existing announcement. This method will set the updated_at field before updating the announcement.
	 *
	 * @param announcement the announcement to be updated
	 * @return the status of the operation
	 */
	CodeStatus updateAnnouncement(Announcement announcement, Integer id);

	/**
	 * Delete an existing announcement. This method will also remove the announcement from all users who have viewed it.
	 *
	 * @param announcement the announcement to be deleted
	 * @return the status of the operation
	 */
	CodeStatus deleteAnnouncement(Integer id);

	/**
	 * Retrieve an announcement by id.
	 *
	 * @param id the id of the announcement to be retrieved
	 * @return an Optional containing the retrieved announcement if found, otherwise an empty Optional
	 */
	Optional<Announcement> getAnnouncement(Integer id);

	/**
	 * Retrieves all {@link Announcement}s sorted and paged according to the given {@link Sort} and {@link Page}.
	 *
	 * @param sort the {@link Sort} to sort the retrieved {@link Announcement}s
	 * @param page the {@link Page} to page the retrieved {@link Announcement}s
	 * @return the list of retrieved {@link Announcement}s
	 */
	List<Announcement> getAllAnnouncements(Sort sort, Page page);

	/**
	 * Searches for announcements by title with optional pagination and sorting.
	 *
	 * @param title the title to search for
	 * @param sort the {@link Sort} to sort the retrieved {@link Announcement}s
	 * @param page the {@link Page} to page the retrieved {@link Announcement}s
	 * @return a list of {@link Announcement}s that match the search criteria
	 */
	List<Announcement> searchAnnouncement(String title, Sort sort, Page page);

	/**
	 * Check if announcement exists by ID
	 *
	 * @param id Announcement ID in the database.
	 * @return true or false
	 */
	boolean isExist(Integer id);
}
