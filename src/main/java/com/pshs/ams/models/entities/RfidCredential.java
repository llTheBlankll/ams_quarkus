package com.pshs.ams.models.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "rfid_credentials")
public class RfidCredential extends PanacheEntityBase {
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
}