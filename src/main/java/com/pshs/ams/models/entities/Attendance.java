package com.pshs.ams.models.entities;

import com.pshs.ams.models.enums.AttendanceStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "attendances")
public class Attendance extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attendances_id_gen")
	@SequenceGenerator(name = "attendances_id_gen", sequenceName = "attendances_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@NotNull
	@ColumnDefault("'ABSENT'")
	@Column(name = "status", nullable = false, length = Integer.MAX_VALUE)
	@Enumerated(EnumType.STRING)
	private AttendanceStatus status;

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
}