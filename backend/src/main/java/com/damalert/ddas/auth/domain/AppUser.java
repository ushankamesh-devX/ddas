package com.damalert.ddas.auth.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_status", nullable = false)
	private AccountStatus accountStatus;

	@Column(name = "preferred_language", nullable = false)
	private String preferredLanguage;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "user_role",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	private Set<Role> roles = new HashSet<>();

	protected AppUser() {
	}

	public AppUser(UUID id, String email, String passwordHash, String displayName) {
		Instant now = Instant.now();
		this.id = id;
		this.email = email.toLowerCase();
		this.passwordHash = passwordHash;
		this.displayName = displayName;
		this.accountStatus = AccountStatus.ACTIVE;
		this.preferredLanguage = "en";
		this.createdAt = now;
		this.updatedAt = now;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getDisplayName() {
		return displayName;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public Set<Role> getRoles() {
		return Set.copyOf(roles);
	}

	public void addRole(Role role) {
		roles.add(role);
	}
}
