package com.pshs.ams.app.users.models.entities;

import com.pshs.ams.app.teachers.models.entities.Teacher;
import com.pshs.ams.global.models.enums.Role;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
	@SequenceGenerator(name = "users_id_gen", sequenceName = "users_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 64)
	@Column(name = "username", length = 64)
	private String username;

	@Size(max = 60)
	@Column(name = "password", length = 60, columnDefinition = "bpchar")
	private String password;

	@Size(max = 128)
	@Column(name = "email", length = 128)
	private String email;

	@Column(name = "profile_picture", length = Integer.MAX_VALUE)
	private String profilePicture;

	@Size(max = 48)
	@ColumnDefault("'GUEST'")
	@Column(name = "role", length = 48)
	@Enumerated(EnumType.STRING)
	private Role role;

	@ColumnDefault("false")
	@Column(name = "is_expired")
	private Boolean isExpired;

	@ColumnDefault("false")
	@Column(name = "is_locked")
	private Boolean isLocked;

	@ColumnDefault("true")
	@Column(name = "is_enabled")
	private Boolean isEnabled;

	@Column(name = "last_login")
	private Instant lastLogin;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at")
	private Instant createdAt;

	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at")
	private Instant updatedAt;

	@OneToOne(mappedBy = "user")
	private Teacher teacher;
}
