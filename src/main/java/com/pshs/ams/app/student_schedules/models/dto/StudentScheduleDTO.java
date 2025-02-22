package com.pshs.ams.app.student_schedules.models.dto;

import com.pshs.ams.app.student_schedules.models.entities.StudentSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * DTO for {@link StudentSchedule}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StudentScheduleDTO implements Serializable {
	private Integer id;
	private String name;
	private LocalTime onTime;
	private LocalTime lateTime;
	private LocalTime absentTime;
	private Boolean isFlag;
}
