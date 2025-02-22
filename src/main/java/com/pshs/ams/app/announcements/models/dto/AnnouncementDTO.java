package com.pshs.ams.app.announcements.models.dto;

import com.pshs.ams.app.announcements.models.entities.Announcement;
import com.pshs.ams.app.users.models.dto.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link Announcement}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementDTO implements Serializable {
	Integer id;
	@NotNull(message = "The title cannot be null.")
	@Size(message = "Title should only be less than 255 characters", min = 1, max = 255)
	@NotEmpty(message = "The title cannot be empty.")
	@NotBlank
	String title;
	@NotNull
	String content;
	@NotNull
	UserDTO user;
	Instant createdAt;
	Instant updatedAt;
}
