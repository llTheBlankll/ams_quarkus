package com.pshs.ams.global.models.custom;

import com.pshs.ams.global.models.enums.CodeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {

	private String message;
	private CodeStatus status;
}
