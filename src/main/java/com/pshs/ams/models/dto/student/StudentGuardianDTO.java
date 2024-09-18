package com.pshs.ams.models.dto.student;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link com.pshs.ams.models.entities.Guardian}
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