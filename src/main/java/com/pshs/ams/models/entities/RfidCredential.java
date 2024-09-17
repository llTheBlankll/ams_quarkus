package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "rfid_credentials")
public class RfidCredential {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rfid_credentials_id_gen")
	@SequenceGenerator(name = "rfid_credentials_id_gen", sequenceName = "rfid_credentials_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@Size(max = 32)
	@NotNull
	@Column(name = "hashed_lrn", nullable = false, length = 32)
	private String hashedLrn;

	@Size(max = 16)
	@NotNull
	@Column(name = "salt", nullable = false, length = 16)
	private String salt;

	public Integer getId() {
		return id;
	}

	public RfidCredential setId(Integer id) {
		this.id = id;
		return this;
	}

	public Student getStudent() {
		return student;
	}

	public RfidCredential setStudent(Student student) {
		this.student = student;
		return this;
	}

	public String getHashedLrn() {
		return hashedLrn;
	}

	public RfidCredential setHashedLrn(String hashedLrn) {
		this.hashedLrn = hashedLrn;
		return this;
	}

	public String getSalt() {
		return salt;
	}

	public RfidCredential setSalt(String salt) {
		this.salt = salt;
		return this;
	}

}