package com.pshs.ams.models.dto.student;

import com.pshs.ams.models.dto.classroom.ClassroomTeacherDTO;
import com.pshs.ams.models.dto.grade_level.GradeLevelDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link com.pshs.ams.models.entities.Classroom}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StudentClassroomDTO implements Serializable {
	private Integer id;
	@NotNull
	@Size(max = 255)
	private String room;
	@NotNull
	@Size(max = 255)
	private String classroomName;
	private ClassroomTeacherDTO teacher;
	@NotNull
	private GradeLevelDTO gradeLevel;
}