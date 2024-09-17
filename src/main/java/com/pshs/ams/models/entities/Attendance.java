package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendances")
public class Attendance {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attendances_id_gen")
	@SequenceGenerator(name = "attendances_id_gen", sequenceName = "attendances_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@NotNull
	@ColumnDefault("'ABSENT'")
	@Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
	private String status;

	@ColumnDefault("CURRENT_DATE")
	@Column(name = "date")
	private LocalDate date;

	@ColumnDefault("LOCALTIME")
	@Column(name = "time_in")
	private LocalTime timeIn;

	@ColumnDefault("LOCALTIME")
	@Column(name = "time_out")
	private LocalTime timeOut;

	@Column(name = "notes", length = Integer.MAX_VALUE)
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "student_id")
	private Student student;

	public Integer getId() {
		return id;
	}

	public Attendance setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public Attendance setStatus(String status) {
		this.status = status;
		return this;
	}

	public LocalDate getDate() {
		return date;
	}

	public Attendance setDate(LocalDate date) {
		this.date = date;
		return this;
	}

	public LocalTime getTimeIn() {
		return timeIn;
	}

	public Attendance setTimeIn(LocalTime timeIn) {
		this.timeIn = timeIn;
		return this;
	}

	public LocalTime getTimeOut() {
		return timeOut;
	}

	public Attendance setTimeOut(LocalTime timeOut) {
		this.timeOut = timeOut;
		return this;
	}

	public String getNotes() {
		return notes;
	}

	public Attendance setNotes(String notes) {
		this.notes = notes;
		return this;
	}

	public Student getStudent() {
		return student;
	}

	public Attendance setStudent(Student student) {
		this.student = student;
		return this;
	}

}