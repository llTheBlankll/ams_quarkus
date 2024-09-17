package com.pshs.ams.models.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "grade_levels")
public class GradeLevel extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "grade_levels_id_gen")
	@SequenceGenerator(name = "grade_levels_id_gen", sequenceName = "grade_levels_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 128)
	@NotNull
	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@Column(name = "description", length = Integer.MAX_VALUE)
	private String description;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@NotNull
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}