package com.pshs.ams.app.students.models.dto;

import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.classrooms.models.dto.ClassroomTeacherDTO;
import com.pshs.ams.app.grade_levels.models.dto.GradeLevelDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link Classroom}
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
