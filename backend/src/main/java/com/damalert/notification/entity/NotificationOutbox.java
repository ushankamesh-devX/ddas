package com.damalert.notification.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationOutbox {
	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID id;

	@Column(name = "alert_id")
	private UUID alertId;

	@Column(name = "recipient_id")
	private UUID recipientId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private Channel channel;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	@Builder.Default
	private OutboxStatus status = OutboxStatus.PENDING;

	@Column(name = "attempt_count", nullable = false)
	@Builder.Default
	private int attemptCount = 0;

	@Column(name = "next_attempt_at", nullable = false)
	private OffsetDateTime nextAttemptAt;

	@Column(name = "last_error", columnDefinition = "TEXT")
	private String lastError;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "processed_at")
	private OffsetDateTime processedAt;
}