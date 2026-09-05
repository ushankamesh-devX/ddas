package com.damalert.ddas.monitoring.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensor_reading")
public class SensorReading {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "sensor_id", nullable = false)
	private java.util.UUID sensorId;

	@Column(name = "measured_at", nullable = false)
	private Instant measuredAt;

	@Column(name = "received_at", nullable = false)
	private Instant receivedAt;

	@Column(nullable = false, precision = 20, scale = 8)
	private BigDecimal value;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private ReadingQuality quality;

	@Column(name = "external_message_id", length = 160)
	private String externalMessageId;

	@Column(name = "raw_payload", columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private String rawPayload;

	protected SensorReading() {
	}

	public SensorReading(java.util.UUID sensorId, Instant measuredAt, BigDecimal value, ReadingQuality quality,
		String externalMessageId, String rawPayload) {
		this.sensorId = sensorId;
		this.measuredAt = measuredAt;
		this.receivedAt = Instant.now();
		this.value = value;
		this.quality = quality;
		this.externalMessageId = externalMessageId;
		this.rawPayload = rawPayload;
	}

	public Long getId() { return id; }
	public java.util.UUID getSensorId() { return sensorId; }
	public Instant getMeasuredAt() { return measuredAt; }
	public Instant getReceivedAt() { return receivedAt; }
	public BigDecimal getValue() { return value; }
	public ReadingQuality getQuality() { return quality; }
	public String getExternalMessageId() { return externalMessageId; }
}
