package com.damalert.ddas.evacuation.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Operational emergency state for a dam. Deliberately separate from
 * {@code dam.operational_state}: alert severity and emergency operational state
 * are related but not identical concepts.
 */
@Entity
@Table(name = "dam_emergency_state")
public class DamEmergencyState {

	@Id
	@Column(name = "dam_id")
	private UUID damId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private EmergencyStatus state;

	@Column(name = "activated_by")
	private UUID activatedBy;

	@Column(name = "activated_at")
	private Instant activatedAt;

	@Column(name = "cleared_by")
	private UUID clearedBy;

	@Column(name = "cleared_at")
	private Instant clearedAt;

	@Column
	private String reason;

	@Column(name = "idempotency_key", length = 100)
	private String idempotencyKey;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected DamEmergencyState() {
	}

	public DamEmergencyState(UUID damId) {
		this.damId = damId;
		this.state = EmergencyStatus.INACTIVE;
		this.updatedAt = Instant.now();
	}

	public void activate(UUID actorUserId, String reason, String idempotencyKey) {
		this.state = EmergencyStatus.ACTIVE;
		this.activatedBy = actorUserId;
		this.activatedAt = Instant.now();
		this.clearedBy = null;
		this.clearedAt = null;
		this.reason = reason;
		this.idempotencyKey = idempotencyKey;
		this.updatedAt = Instant.now();
	}

	public void clear(UUID actorUserId, String reason, String idempotencyKey) {
		this.state = EmergencyStatus.INACTIVE;
		this.clearedBy = actorUserId;
		this.clearedAt = Instant.now();
		this.reason = reason;
		this.idempotencyKey = idempotencyKey;
		this.updatedAt = Instant.now();
	}

	public boolean isActive() {
		return state == EmergencyStatus.ACTIVE;
	}

	public UUID getDamId() {
		return damId;
	}

	public EmergencyStatus getState() {
		return state;
	}

	public UUID getActivatedBy() {
		return activatedBy;
	}

	public Instant getActivatedAt() {
		return activatedAt;
	}

	public UUID getClearedBy() {
		return clearedBy;
	}

	public Instant getClearedAt() {
		return clearedAt;
	}

	public String getReason() {
		return reason;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
