package com.pshs.ams.app.grade_levels.models.dto;

import com.pshs.ams.app.grade_levels.models.entities.GradeLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link GradeLevel}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GradeLevelDTO implements Serializable {
	private Integer id;
	@NotNull
	@Size(max = 128)
	private String name;
	private String description;
}
