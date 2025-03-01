package com.pshs.ams.app.classrooms.models.entities;

import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.app.teachers.models.entities.Teacher;
import com.pshs.ams.app.grade_levels.models.entities.GradeLevel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "classrooms")
public class Classroom extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classrooms_id_gen")
	@SequenceGenerator(name = "classrooms_id_gen", sequenceName = "classrooms_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 255)
	@NotNull
	@Column(name = "room", nullable = false)
	private String room;

	@Size(max = 255)
	@NotNull
	@Column(name = "classroom_name", nullable = false)
	private String classroomName;

	@Size(max = 255)
	@Column(name = "profile_picture")
	private String profilePicture;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "teacher_id")
	private Teacher teacher;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "grade_level_id", nullable = false)
	private GradeLevel gradeLevel;

	@OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
	private Set<Student> students = new LinkedHashSet<>();

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
