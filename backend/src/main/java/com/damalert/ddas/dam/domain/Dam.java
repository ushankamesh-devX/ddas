package com.damalert.ddas.dam.domain;

import java.time.Instant;
import java.util.UUID;

import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dam")
public class Dam {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "operational_state", nullable = false)
	private DamOperationalState operationalState;

	@Column(name = "public_status_message")
	private String publicStatusMessage;

	@Column(columnDefinition = "geometry(Polygon,4326)")
	private Polygon area;

	@Column(name = "is_public", nullable = false)
	private boolean publicVisible;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Dam() {
	}

	public Dam(UUID id, String code, String name, String description, boolean publicVisible) {
		Instant now = Instant.now();
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.operationalState = DamOperationalState.NORMAL;
		this.publicVisible = publicVisible;
		this.active = true;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public DamOperationalState getOperationalState() {
		return operationalState;
	}

	public String getPublicStatusMessage() {
		return publicStatusMessage;
	}

	public boolean isPublicVisible() {
		return publicVisible;
	}

	public boolean isActive() {
		return active;
	}

	public Polygon getArea() { return area; }

	public void update(String name, String description, String publicStatusMessage, Polygon area, boolean publicVisible) {
		this.name = name;
		this.description = description;
		this.publicStatusMessage = publicStatusMessage;
		this.area = area;
		this.publicVisible = publicVisible;
		this.updatedAt = Instant.now();
	}

	public void setOperationalState(DamOperationalState state, String publicStatusMessage) {
		this.operationalState = state;
		this.publicStatusMessage = publicStatusMessage;
		this.updatedAt = Instant.now();
	}

	public void deactivate() {
		this.active = false;
		this.updatedAt = Instant.now();
	}
}
