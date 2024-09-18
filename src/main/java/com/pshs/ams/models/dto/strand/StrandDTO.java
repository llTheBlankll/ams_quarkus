package com.pshs.ams.models.dto.strand;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link com.pshs.ams.models.entities.Strand}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StrandDTO implements Serializable {
	private Integer id;
	@NotNull
	@Size(max = 255)
	private String name;
	private String description;
}