package com.pshs.ams.models.dto.custom;

import com.pshs.ams.models.enums.AttendanceMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RFIDCardDTO {
	private String hashedLrn;
	private AttendanceMode mode;
}