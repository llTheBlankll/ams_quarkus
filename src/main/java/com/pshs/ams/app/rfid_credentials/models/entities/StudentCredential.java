package com.pshs.ams.app.rfid_credentials.models.entities;

import com.pshs.ams.app.students.models.entities.Student;
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
@Table(name = "student_credentials")
public class StudentCredential extends PanacheEntityBase {
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
	@Column(name = "hashed_lrn", nullable = false, length = 32, columnDefinition = "bpchar")
	private String hashedLrn;

	@Column(name = "fingerprint_id")
	private Integer fingerprintId;

	@Size(max = 16)
	@NotNull
	@Column(name = "salt", nullable = false, length = 16)
	private String salt;
}
