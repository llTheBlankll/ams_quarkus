package com.pshs.ams.app.classrooms.models.dto;

import com.pshs.ams.app.teachers.models.entities.Teacher;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * DTO for {@link Teacher}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ClassroomTeacherDTO implements Serializable {
	private Integer id;
	@Size(max = 32)
	private String firstName;
	@Size(max = 32)
	private String lastName;
	@Size(max = 4)
	private String middleInitial;
	private Integer age;
	@Size(max = 32)
	private String contactNumber;
	@Size(max = 32)
	private String emergencyContact;
	@Size(max = 16)
	private String sex;
	@Size(max = 128)
	private String position;
}
