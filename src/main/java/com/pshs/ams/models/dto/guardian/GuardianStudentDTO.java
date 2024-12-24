package com.pshs.ams.models.dto.guardian;

import com.pshs.ams.models.dto.grade_level.GradeLevelDTO;
import com.pshs.ams.models.dto.strand.StrandDTO;
import com.pshs.ams.models.dto.student.StudentClassroomDTO;
import com.pshs.ams.models.dto.student_schedules.StudentScheduleDTO;
import com.pshs.ams.models.enums.Sex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.pshs.ams.models.entities.Student}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GuardianStudentDTO implements Serializable {
	private Long id;
	@NotNull
	@Size(max = 128)
	private String firstName;
	@Size(max = 8)
	private String middleInitial;
	@NotNull
	@Size(max = 128)
	private String lastName;
	@Size(max = 8)
	private String prefix;
	private String address;
	private Sex sex;
	@NotNull
	private LocalDate birthdate;
	private StudentClassroomDTO classroom;
	private GradeLevelDTO gradeLevel;
	private StrandDTO strand;
	private StudentScheduleDTO studentSchedule;
}
