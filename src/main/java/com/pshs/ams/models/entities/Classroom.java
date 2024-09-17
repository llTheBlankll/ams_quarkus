package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "classrooms")
public class Classroom {
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

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "teacher_id")
	private Teacher teacher;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "grade_level_id", nullable = false)
	private GradeLevel gradeLevel;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "classroom")
	private Set<Student> students = new LinkedHashSet<>();

	public Integer getId() {
		return id;
	}

	public Classroom setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getRoom() {
		return room;
	}

	public Classroom setRoom(String room) {
		this.room = room;
		return this;
	}

	public String getClassroomName() {
		return classroomName;
	}

	public Classroom setClassroomName(String classroomName) {
		this.classroomName = classroomName;
		return this;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public Classroom setTeacher(Teacher teacher) {
		this.teacher = teacher;
		return this;
	}

	public GradeLevel getGradeLevel() {
		return gradeLevel;
	}

	public Classroom setGradeLevel(GradeLevel gradeLevel) {
		this.gradeLevel = gradeLevel;
		return this;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Classroom setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Classroom setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public Set<Student> getStudents() {
		return students;
	}

	public Classroom setStudents(Set<Student> students) {
		this.students = students;
		return this;
	}

}