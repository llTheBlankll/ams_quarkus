package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "strands")
public class Strand {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "strands_id_gen")
	@SequenceGenerator(name = "strands_id_gen", sequenceName = "strands_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 255)
	@NotNull
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", length = Integer.MAX_VALUE)
	private String description;

	public Integer getId() {
		return id;
	}

	public Strand setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Strand setName(String name) {
		this.name = name;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Strand setDescription(String description) {
		this.description = description;
		return this;
	}

}