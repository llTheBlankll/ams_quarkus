package com.pshs.ams.models.dto.classroom;

import com.pshs.ams.models.dto.grade_level.GradeLevelDTO;
import com.pshs.ams.models.dto.student.StudentDTO;
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
 * DTO for {@link com.pshs.ams.models.entities.Classroom}
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
	private ClassroomTeacherDTO teacher;
	@NotNull
	private GradeLevelDTO gradeLevel;
	private Set<ClassroomStudentDTO> students = new LinkedHashSet<>();
}