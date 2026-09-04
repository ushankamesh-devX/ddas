package com.damalert.ddas.dam.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "dam_staff")
public class DamStaff {

	@EmbeddedId
	private DamStaffId id;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_code", nullable = false)
	private DamStaffRole role;

	@Column(name = "can_trigger_emergency", nullable = false)
	private boolean canTriggerEmergency;

	@Column(name = "assigned_at", nullable = false)
	private Instant assignedAt;

	protected DamStaff() {
	}

	public DamStaff(DamStaffId id, DamStaffRole role, boolean canTriggerEmergency) {
		this.id = id;
		this.role = role;
		this.canTriggerEmergency = canTriggerEmergency;
		this.assignedAt = Instant.now();
	}

	public DamStaffId getId() {
		return id;
	}

	public DamStaffRole getRole() {
		return role;
	}

	public boolean canTriggerEmergency() {
		return canTriggerEmergency;
	}

	public Instant getAssignedAt() {
		return assignedAt;
	}
}
