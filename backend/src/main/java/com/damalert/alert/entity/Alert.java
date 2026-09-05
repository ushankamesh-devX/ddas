package com.damalert.alert.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alert")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "dam_id", nullable = false)
	private UUID damId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private AlertSeverity severity;

	@Column(nullable = false, length = 220)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "recommended_action", columnDefinition = "TEXT")
	private String recommendedAction;

	@Column(name = "evacuation_required", nullable = false)
	private boolean evacuationRequired;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	@Builder.Default
	private AlertStatus status = AlertStatus.ACTIVE;

	@Column(name = "created_by")
	private UUID createdBy;

	@Column(name = "cancelled_by")
	private UUID cancelledBy;

	@Column(name = "cancelled_at")
	private OffsetDateTime cancelledAt;

	@Column(name = "expires_at")
	private OffsetDateTime expiresAt;

	@Column(name = "idempotency_key", length = 100)
	private String idempotencyKey;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;
}