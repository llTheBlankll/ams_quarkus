package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "guardians")
public class Guardian {
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

	public Integer getId() {
		return id;
	}

	public Guardian setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getFullName() {
		return fullName;
	}

	public Guardian setFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public Guardian setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
		return this;
	}

	public Set<Student> getStudents() {
		return students;
	}

	public Guardian setStudents(Set<Student> students) {
		this.students = students;
		return this;
	}

}