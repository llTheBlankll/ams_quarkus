package com.pshs.ams.models.dto.attendance;

import com.pshs.ams.models.dto.student.StudentDTO;
import com.pshs.ams.models.enums.AttendanceStatus;
import com.pshs.ams.models.interfaces.AttendanceForeignEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link com.pshs.ams.models.entities.Attendance}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AttendanceDTO implements Serializable {
	private Integer id;
	@NotNull
	private AttendanceStatus status;
	private LocalDate date;
	private LocalTime timeIn;
	private LocalTime timeOut;
	private String notes;
	private StudentDTO student;
}