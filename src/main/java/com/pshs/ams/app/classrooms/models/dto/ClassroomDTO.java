package com.pshs.ams.app.classrooms.models.dto;

import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.grade_levels.models.dto.GradeLevelDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link Classroom}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ClassroomDTO implements Serializable {
	private Integer id;
	@NotNull
	@Size(max = 255)
	private String room;
	@NotNull
	@Size(max = 255)
	private String classroomName;
	private String profilePicture;
	private ClassroomTeacherDTO teacher;
	@NotNull
	private GradeLevelDTO gradeLevel;
	private Set<ClassroomStudentDTO> students = new LinkedHashSet<>();
}
