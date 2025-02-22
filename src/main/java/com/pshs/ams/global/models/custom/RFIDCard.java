package com.pshs.ams.global.models.custom;

import com.pshs.ams.app.attendances.models.enums.AttendanceMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RFIDCard {
	private String hashedLrn;
	private AttendanceMode mode;
}
