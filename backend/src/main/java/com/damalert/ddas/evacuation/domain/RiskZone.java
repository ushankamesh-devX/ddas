package com.damalert.ddas.evacuation.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "risk_zone")
public class RiskZone {

	@Id
	private UUID id;

	@Column(name = "dam_id", nullable = false)
	private UUID damId;

	@Column(nullable = false, length = 100)
	private String code;

	@Column(nullable = false, length = 180)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private RiskZoneSeverity severity;

	@Column(nullable = false)
	private Polygon geometry;

	@Column(name = "evacuation_required", nullable = false)
	private boolean evacuationRequired;

	@Column(name = "public_visible", nullable = false)
	private boolean publicVisible;

	@Column
	private String instructions;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected RiskZone() {
	}

	public RiskZone(
		UUID damId,
		String code,
		String name,
		RiskZoneSeverity severity,
		Polygon geometry,
		boolean evacuationRequired,
		boolean publicVisible,
		String instructions
	) {
		Instant now = Instant.now();
		this.id = UUID.randomUUID();
		this.damId = damId;
		this.code = code;
		this.name = name;
		this.severity = severity;
		this.geometry = geometry;
		this.evacuationRequired = evacuationRequired;
		this.publicVisible = publicVisible;
		this.instructions = instructions;
		this.active = true;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void update(
		String name,
		RiskZoneSeverity severity,
		Polygon geometry,
		Boolean evacuationRequired,
		Boolean publicVisible,
		String instructions,
		Boolean active
	) {
		if (name != null) {
			this.name = name;
		}
		if (severity != null) {
			this.severity = severity;
		}
		if (geometry != null) {
			this.geometry = geometry;
		}
		if (evacuationRequired != null) {
			this.evacuationRequired = evacuationRequired;
		}
		if (publicVisible != null) {
			this.publicVisible = publicVisible;
		}
		if (instructions != null) {
			this.instructions = instructions;
		}
		if (active != null) {
			this.active = active;
		}
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getDamId() {
		return damId;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public RiskZoneSeverity getSeverity() {
		return severity;
	}

	public Polygon getGeometry() {
		return geometry;
	}

	public boolean isEvacuationRequired() {
		return evacuationRequired;
	}

	public boolean isPublicVisible() {
		return publicVisible;
	}

	public String getInstructions() {
		return instructions;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
