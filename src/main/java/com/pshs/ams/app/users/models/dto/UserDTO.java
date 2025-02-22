package com.pshs.ams.app.users.models.dto;

import com.pshs.ams.app.users.models.entities.User;
import com.pshs.ams.global.models.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link User}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserDTO implements Serializable {
	private Integer id;
	@Size(max = 64)
	private String username;
	@Size(max = 128)
	private String email;
	private String profilePicture;
	private Role role;
	private Boolean isExpired;
	private Boolean isLocked;
	private Boolean isEnabled;
	private Instant lastLogin;
	private UserTeacherDTO teacher;
}
