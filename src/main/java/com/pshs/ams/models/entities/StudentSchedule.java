package com.pshs.ams.models.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "student_schedules")
public class StudentSchedule extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "student_schedules_id_gen")
	@SequenceGenerator(name = "student_schedules_id_gen", sequenceName = "student_schedules_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "name")
	private String name;

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
}
