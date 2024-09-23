package com.pshs.ams.models.dto.teacher;

import com.pshs.ams.models.enums.AttendanceStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.pshs.ams.models.entities.User}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TeacherUserDTO implements Serializable {
	private Integer id;
	@Size(max = 64)
	private String username;
	@Size(max = 60)
	private String password;
	@Size(max = 128)
	private String email;
	private String profilePicture;
	private AttendanceStatus role;
	private Boolean isExpired;
	private Boolean isLocked;
	private Boolean isEnabled;
	private Instant lastLogin;
}