package com.pshs.ams.models.dto.custom;

import com.pshs.ams.models.enums.CodeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

	private String message;
	private CodeStatus status;
}