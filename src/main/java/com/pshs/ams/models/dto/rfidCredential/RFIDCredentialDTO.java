package com.pshs.ams.models.dto.rfidCredential;

import com.pshs.ams.models.dto.student.StudentDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link com.pshs.ams.models.entities.RfidCredential}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RFIDCredentialDTO implements Serializable {
	private Integer id;
	@NotNull
	private StudentDTO student;
	@NotNull
	@Size(max = 32)
	private String hashedLrn;
	@NotNull
	@Size(max = 16)
	private String salt;
}