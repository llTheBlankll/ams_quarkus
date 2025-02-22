package com.pshs.ams.app.attendances.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pshs.ams.app.attendances.models.entities.Attendance;
import com.pshs.ams.app.students.models.dto.StudentDTO;
import com.pshs.ams.app.attendances.models.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link Attendance}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AttendanceDTO implements Serializable {
	private Integer id;
	@NotNull
	private AttendanceStatus status;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime timeIn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
	private LocalTime timeOut;
	private String notes;
	private StudentDTO student;
}
