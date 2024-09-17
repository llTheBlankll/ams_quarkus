package com.pshs.ams.models.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
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

	@Size(max = 8)
	@Column(name = "prefix", length = 8)
	private String prefix;

	@Column(name = "address", length = Integer.MAX_VALUE)
	private String address;

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

/*
 TODO [Reverse Engineering] create field to map the 'sex' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "sex", columnDefinition = "sex not null")
    private Object sex;
*/
}