package com.pshs.ams.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
	@SequenceGenerator(name = "users_id_gen", sequenceName = "users_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Size(max = 64)
	@Column(name = "username", length = 64)
	private String username;

	@Size(max = 60)
	@Column(name = "password", length = 60)
	private String password;

	@Size(max = 128)
	@Column(name = "email", length = 128)
	private String email;

	@Column(name = "profile_picture", length = Integer.MAX_VALUE)
	private String profilePicture;

	@Size(max = 48)
	@ColumnDefault("'GUEST'")
	@Column(name = "role", length = 48)
	private String role;

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
	private Teacher teachers;

	public Integer getId() {
		return id;
	}

	public User setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public User setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public User setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public User setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public User setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
		return this;
	}

	public String getRole() {
		return role;
	}

	public User setRole(String role) {
		this.role = role;
		return this;
	}

	public Boolean getIsExpired() {
		return isExpired;
	}

	public User setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
		return this;
	}

	public Boolean getIsLocked() {
		return isLocked;
	}

	public User setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
		return this;
	}

	public Boolean getIsEnabled() {
		return isEnabled;
	}

	public User setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
		return this;
	}

	public Instant getLastLogin() {
		return lastLogin;
	}

	public User setLastLogin(Instant lastLogin) {
		this.lastLogin = lastLogin;
		return this;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public User setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public User setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public Teacher getTeachers() {
		return teachers;
	}

	public User setTeachers(Teacher teachers) {
		this.teachers = teachers;
		return this;
	}

}