package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "grade_levels")
public class GradeLevel {
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

	public Integer getId() {
		return id;
	}

	public GradeLevel setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public GradeLevel setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public GradeLevel setDescription(String description) {
		this.description = description;
		return this;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public GradeLevel setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public GradeLevel setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

}