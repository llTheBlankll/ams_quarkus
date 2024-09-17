package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "student_schedules")
public class StudentSchedule {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_schedules_id_gen")
	@SequenceGenerator(name = "student_schedules_id_gen", sequenceName = "student_schedules_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "on_time")
	private LocalTime onTime;

	@Column(name = "late_time")
	private LocalTime lateTime;

	@Column(name = "absent_time")
	private LocalTime absentTime;

	@Column(name = "is_flag")
	private Boolean isFlag;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at")
	private Instant createdAt;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at")
	private Instant updatedAt;

	public Integer getId() {
		return id;
	}

	public StudentSchedule setId(Integer id) {
		this.id = id;
		return this;
	}

	public LocalTime getOnTime() {
		return onTime;
	}

	public StudentSchedule setOnTime(LocalTime onTime) {
		this.onTime = onTime;
		return this;
	}

	public LocalTime getLateTime() {
		return lateTime;
	}

	public StudentSchedule setLateTime(LocalTime lateTime) {
		this.lateTime = lateTime;
		return this;
	}

	public LocalTime getAbsentTime() {
		return absentTime;
	}

	public StudentSchedule setAbsentTime(LocalTime absentTime) {
		this.absentTime = absentTime;
		return this;
	}

	public Boolean getIsFlag() {
		return isFlag;
	}

	public StudentSchedule setIsFlag(Boolean isFlag) {
		this.isFlag = isFlag;
		return this;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public StudentSchedule setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public StudentSchedule setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

}