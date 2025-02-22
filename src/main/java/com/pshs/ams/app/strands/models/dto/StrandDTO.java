package com.pshs.ams.app.strands.models.dto;

import com.pshs.ams.app.strands.models.entities.Strand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link Strand}
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
