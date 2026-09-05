package com.damalert.ddas.monitoring.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensor")
public class Sensor {

	@Id
	private UUID id;

	@Column(name = "dam_id", nullable = false)
	private UUID damId;

	@Column(nullable = false, length = 100)
	private String code;

	@Column(nullable = false, length = 180)
	private String name;

	@Column(name = "sensor_type", nullable = false, length = 48)
	private String sensorType;

	@Column(nullable = false, length = 32)
	private String unit;

	@Column(columnDefinition = "geometry(Point,4326)")
	private Point location;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private SensorVisibility visibility;

	@Column(name = "expose_exact_location", nullable = false)
	private boolean exposeExactLocation;

	@Column(name = "warning_threshold", precision = 18, scale = 6)
	private BigDecimal warningThreshold;

	@Column(name = "critical_threshold", precision = 18, scale = 6)
	private BigDecimal criticalThreshold;

	@Enumerated(EnumType.STRING)
	@Column(name = "threshold_direction", nullable = false, length = 8)
	private ThresholdDirection thresholdDirection;

	@Enumerated(EnumType.STRING)
	@Column(name = "sensor_status", nullable = false, length = 24)
	private SensorStatus status;

	@Column(name = "last_seen_at")
	private Instant lastSeenAt;

	@Column(nullable = false, columnDefinition = "jsonb")
	@JdbcTypeCode(SqlTypes.JSON)
	private String metadata;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Sensor() {
	}

	public Sensor(UUID id, UUID damId, String code, String name, String sensorType, String unit, Point location,
		SensorVisibility visibility, boolean exposeExactLocation, BigDecimal warningThreshold,
		BigDecimal criticalThreshold, ThresholdDirection thresholdDirection) {
		Instant now = Instant.now();
		this.id = id;
		this.damId = damId;
		this.code = code;
		this.name = name;
		this.sensorType = sensorType;
		this.unit = unit;
		this.location = location;
		this.visibility = visibility;
		this.exposeExactLocation = exposeExactLocation;
		this.warningThreshold = warningThreshold;
		this.criticalThreshold = criticalThreshold;
		this.thresholdDirection = thresholdDirection;
		this.status = SensorStatus.ACTIVE;
		this.metadata = "{}";
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void update(String name, String sensorType, String unit, Point location, SensorVisibility visibility,
		boolean exposeExactLocation, BigDecimal warningThreshold, BigDecimal criticalThreshold,
		ThresholdDirection thresholdDirection, SensorStatus status) {
		this.name = name;
		this.sensorType = sensorType;
		this.unit = unit;
		this.location = location;
		this.visibility = visibility;
		this.exposeExactLocation = exposeExactLocation;
		this.warningThreshold = warningThreshold;
		this.criticalThreshold = criticalThreshold;
		this.thresholdDirection = thresholdDirection;
		this.status = status;
		this.updatedAt = Instant.now();
	}

	public void seenAt(Instant measuredAt) {
		if (lastSeenAt == null || measuredAt.isAfter(lastSeenAt)) {
			lastSeenAt = measuredAt;
		}
		if (status == SensorStatus.OFFLINE) {
			status = SensorStatus.ACTIVE;
		}
		updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public UUID getDamId() { return damId; }
	public String getCode() { return code; }
	public String getName() { return name; }
	public String getSensorType() { return sensorType; }
	public String getUnit() { return unit; }
	public Point getLocation() { return location; }
	public SensorVisibility getVisibility() { return visibility; }
	public boolean isExposeExactLocation() { return exposeExactLocation; }
	public BigDecimal getWarningThreshold() { return warningThreshold; }
	public BigDecimal getCriticalThreshold() { return criticalThreshold; }
	public ThresholdDirection getThresholdDirection() { return thresholdDirection; }
	public SensorStatus getStatus() { return status; }
	public Instant getLastSeenAt() { return lastSeenAt; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
