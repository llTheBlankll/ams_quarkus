package com.pshs.ams.app.students.models.entities;

import com.pshs.ams.app.guardians.models.entities.Guardian;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.grade_levels.models.entities.GradeLevel;
import com.pshs.ams.app.strands.models.entities.Strand;
import com.pshs.ams.app.student_schedules.models.entities.StudentSchedule;
import com.pshs.ams.global.models.enums.Sex;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student extends PanacheEntityBase {
	@Id
	@Column(name = "id", nullable = false)
	private Long id;

	@Size(max = 128)
	@NotNull
	@Column(name = "first_name", nullable = false, length = 128)
	private String firstName;

	@Size(max = 8)
	@Column(name = "middle_initial", length = 8)
	private String middleInitial;

	@Size(max = 128)
	@NotNull
	@Column(name = "last_name", nullable = false, length = 128)
	private String lastName;

	@Size(max = 255)
	@Column(name = "profile_picture", nullable = true, length = 255)
	private String profilePicture;

	@Size(max = 8)
	@Column(name = "prefix", length = 8)
	private String prefix;

	@Column(name = "address", length = Integer.MAX_VALUE)
	private String address;

	@Column(name = "sex")
	@Enumerated(EnumType.STRING)
	private Sex sex;

	@NotNull
	@Column(name = "birthdate", nullable = false)
	private LocalDate birthdate;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "classroom_id")
	private Classroom classroom;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "grade_level_id")
	private GradeLevel gradeLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "strand_id")
	private Strand strand;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "guardian_id")
	private Guardian guardian;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "student_schedule_id")
	private StudentSchedule studentSchedule;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
