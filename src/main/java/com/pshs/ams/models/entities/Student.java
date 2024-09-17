package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {
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

	public Long getId() {
		return id;
	}

	public Student setId(Long id) {
		this.id = id;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public Student setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getMiddleInitial() {
		return middleInitial;
	}

	public Student setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public Student setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getPrefix() {
		return prefix;
	}

	public Student setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public Student setAddress(String address) {
		this.address = address;
		return this;
	}

	public LocalDate getBirthdate() {
		return birthdate;
	}

	public Student setBirthdate(LocalDate birthdate) {
		this.birthdate = birthdate;
		return this;
	}

	public Classroom getClassroom() {
		return classroom;
	}

	public Student setClassroom(Classroom classroom) {
		this.classroom = classroom;
		return this;
	}

	public GradeLevel getGradeLevel() {
		return gradeLevel;
	}

	public Student setGradeLevel(GradeLevel gradeLevel) {
		this.gradeLevel = gradeLevel;
		return this;
	}

	public Strand getStrand() {
		return strand;
	}

	public Student setStrand(Strand strand) {
		this.strand = strand;
		return this;
	}

	public Guardian getGuardian() {
		return guardian;
	}

	public Student setGuardian(Guardian guardian) {
		this.guardian = guardian;
		return this;
	}

	public StudentSchedule getStudentSchedule() {
		return studentSchedule;
	}

	public Student setStudentSchedule(StudentSchedule studentSchedule) {
		this.studentSchedule = studentSchedule;
		return this;
	}

/*
 TODO [Reverse Engineering] create field to map the 'sex' column
 Available actions: Define target Java type | Uncomment as is | Remove column mapping
    @Column(name = "sex", columnDefinition = "sex not null")
    private Object sex;
*/
}