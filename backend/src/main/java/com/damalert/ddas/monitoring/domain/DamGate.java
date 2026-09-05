package com.damalert.ddas.monitoring.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dam_gate")
public class DamGate {

	@Id
	private UUID id;
	@Column(name = "dam_id", nullable = false)
	private UUID damId;
	@Column(nullable = false, length = 100)
	private String code;
	@Column(nullable = false, length = 180)
	private String name;
	@Column(columnDefinition = "geometry(Point,4326)")
	private Point location;
	@Enumerated(EnumType.STRING)
	@Column(name = "gate_status", nullable = false, length = 24)
	private GateStatus status;
	@Column(name = "opening_percent", precision = 5, scale = 2)
	private BigDecimal openingPercent;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private SensorVisibility visibility;
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected DamGate() {
	}

	public DamGate(UUID id, UUID damId, String code, String name, Point location, GateStatus status,
		BigDecimal openingPercent, SensorVisibility visibility) {
		this.id = id;
		this.damId = damId;
		this.code = code;
		this.name = name;
		this.location = location;
		this.status = status;
		this.openingPercent = openingPercent;
		this.visibility = visibility;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public void update(String name, Point location, GateStatus status, BigDecimal openingPercent,
		SensorVisibility visibility) {
		this.name = name;
		this.location = location;
		this.status = status;
		this.openingPercent = openingPercent;
		this.visibility = visibility;
		this.updatedAt = Instant.now();
	}

	public UUID getId() { return id; }
	public UUID getDamId() { return damId; }
	public String getCode() { return code; }
	public String getName() { return name; }
	public Point getLocation() { return location; }
	public GateStatus getStatus() { return status; }
	public BigDecimal getOpeningPercent() { return openingPercent; }
	public SensorVisibility getVisibility() { return visibility; }
	public Instant getUpdatedAt() { return updatedAt; }
}
