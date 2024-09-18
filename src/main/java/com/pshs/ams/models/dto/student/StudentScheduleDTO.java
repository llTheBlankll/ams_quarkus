package com.pshs.ams.models.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * DTO for {@link com.pshs.ams.models.entities.StudentSchedule}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StudentScheduleDTO implements Serializable {
	private Integer id;
	private LocalTime onTime;
	private LocalTime lateTime;
	private LocalTime absentTime;
	private Boolean isFlag;
}