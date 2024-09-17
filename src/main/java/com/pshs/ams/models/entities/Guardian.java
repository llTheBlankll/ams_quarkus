package com.pshs.ams.models.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "guardians")
public class Guardian extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "guardians_id_gen")
	@SequenceGenerator(name = "guardians_id_gen", sequenceName = "guardians_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 128)
	@NotNull
	@Column(name = "full_name", nullable = false, length = 128)
	private String fullName;

	@Size(max = 32)
	@Column(name = "contact_number", length = 32)
	private String contactNumber;

	@OneToMany(mappedBy = "guardian")
	private Set<Student> students = new LinkedHashSet<>();
}