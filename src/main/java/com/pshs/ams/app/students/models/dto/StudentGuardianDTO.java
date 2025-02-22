package com.pshs.ams.app.students.models.dto;

import com.pshs.ams.app.guardians.models.entities.Guardian;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link Guardian}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StudentGuardianDTO implements Serializable {
	private Integer id;
	@NotNull
	@Size(max = 128)
	private String fullName;
	@Size(max = 32)
	private String contactNumber;
}
