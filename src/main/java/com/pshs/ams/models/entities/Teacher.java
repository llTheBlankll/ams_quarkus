package com.pshs.ams.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "teachers")
public class Teacher extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teachers_id_gen")
	@SequenceGenerator(name = "teachers_id_gen", sequenceName = "teachers_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 32)
	@Column(name = "first_name", length = 32)
	private String firstName;

	@Size(max = 32)
	@Column(name = "last_name", length = 32)
	private String lastName;

	@Size(max = 4)
	@Column(name = "middle_initial", length = 4)
	private String middleInitial;

	@Size(max = 255)
	@Column(name = "profile_picture")
	private String profilePicture;

	@Column(name = "age")
	private Integer age;

	@Size(max = 32)
	@Column(name = "contact_number", length = 32)
	private String contactNumber;

	@Size(max = 32)
	@Column(name = "emergency_contact", length = 32)
	private String emergencyContact;

	@Size(max = 16)
	@Column(name = "sex", length = 16)
	private String sex;

	@Size(max = 128)
	@Column(name = "position", length = 128)
	private String position;

	@OneToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.SET_NULL)
	@JoinColumn(name = "user_id")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private User user;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at")
	private Instant createdAt;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at")
	private Instant updatedAt;
}