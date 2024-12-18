package com.pshs.ams.models.dto.announcement;

import com.pshs.ams.models.dto.user.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.pshs.ams.models.entities.Announcement}
 */
@Value
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
