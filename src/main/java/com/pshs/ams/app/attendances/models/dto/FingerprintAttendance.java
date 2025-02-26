package com.pshs.ams.app.attendances.models.dto;

import com.pshs.ams.app.attendances.models.enums.AttendanceMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FingerprintAttendance {
	private Integer fingerprintId;
	private LocalDateTime dateTime;
	private AttendanceMode mode;
}
