package com.damalert.ddas.evacuation.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.locationtech.jts.geom.LineString;

@Entity
@Table(name = "evacuation_route")
public class EvacuationRoute {

	@Id
	private UUID id;

	@Column(name = "dam_id", nullable = false)
	private UUID damId;

	@Column(nullable = false, length = 100)
	private String code;

	@Column(nullable = false, length = 180)
	private String name;

	@Column(name = "from_zone_id", nullable = false)
	private UUID fromZoneId;

	@Column(name = "safe_location_id", nullable = false)
	private UUID safeLocationId;

	@Column(nullable = false)
	private LineString geometry;

	@Enumerated(EnumType.STRING)
	@Column(name = "route_status", nullable = false, length = 24)
	private RouteStatus routeStatus;

	@Column(name = "public_visible", nullable = false)
	private boolean publicVisible;

	@Column
	private String instructions;

	@Column(nullable = false)
	private short priority;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected EvacuationRoute() {
	}

	public EvacuationRoute(
		UUID damId,
		String code,
		String name,
		UUID fromZoneId,
		UUID safeLocationId,
		LineString geometry,
		RouteStatus routeStatus,
		boolean publicVisible,
		String instructions,
		short priority
	) {
		Instant now = Instant.now();
		this.id = UUID.randomUUID();
		this.damId = damId;
		this.code = code;
		this.name = name;
		this.fromZoneId = fromZoneId;
		this.safeLocationId = safeLocationId;
		this.geometry = geometry;
		this.routeStatus = routeStatus;
		this.publicVisible = publicVisible;
		this.instructions = instructions;
		this.priority = priority;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void update(
		String name,
		UUID fromZoneId,
		UUID safeLocationId,
		LineString geometry,
		RouteStatus routeStatus,
		Boolean publicVisible,
		String instructions,
		Short priority
	) {
		if (name != null) {
			this.name = name;
		}
		if (fromZoneId != null) {
			this.fromZoneId = fromZoneId;
		}
		if (safeLocationId != null) {
			this.safeLocationId = safeLocationId;
		}
		if (geometry != null) {
			this.geometry = geometry;
		}
		if (routeStatus != null) {
			this.routeStatus = routeStatus;
		}
		if (publicVisible != null) {
			this.publicVisible = publicVisible;
		}
		if (instructions != null) {
			this.instructions = instructions;
		}
		if (priority != null) {
			this.priority = priority;
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

	public UUID getFromZoneId() {
		return fromZoneId;
	}

	public UUID getSafeLocationId() {
		return safeLocationId;
	}

	public LineString getGeometry() {
		return geometry;
	}

	public RouteStatus getRouteStatus() {
		return routeStatus;
	}

	public boolean isPublicVisible() {
		return publicVisible;
	}

	public String getInstructions() {
		return instructions;
	}

	public short getPriority() {
		return priority;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
