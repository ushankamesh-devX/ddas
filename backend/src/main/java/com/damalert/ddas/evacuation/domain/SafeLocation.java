package com.damalert.ddas.evacuation.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "safe_location")
public class SafeLocation {

	@Id
	private UUID id;

	@Column(name = "dam_id", nullable = false)
	private UUID damId;

	@Column(nullable = false, length = 100)
	private String code;

	@Column(nullable = false, length = 180)
	private String name;

	@Column(nullable = false)
	private Point location;

	@Column
	private Integer capacity;

	@Column(name = "current_occupancy")
	private Integer currentOccupancy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private SafeLocationStatus status;

	@Column(name = "contact_number", length = 32)
	private String contactNumber;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private List<String> facilities;

	@Column(name = "public_visible", nullable = false)
	private boolean publicVisible;

	@Column
	private String instructions;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected SafeLocation() {
	}

	public SafeLocation(
		UUID damId,
		String code,
		String name,
		Point location,
		Integer capacity,
		Integer currentOccupancy,
		SafeLocationStatus status,
		String contactNumber,
		List<String> facilities,
		boolean publicVisible,
		String instructions
	) {
		Instant now = Instant.now();
		this.id = UUID.randomUUID();
		this.damId = damId;
		this.code = code;
		this.name = name;
		this.location = location;
		this.capacity = capacity;
		this.currentOccupancy = currentOccupancy;
		this.status = status;
		this.contactNumber = contactNumber;
		this.facilities = facilities == null ? List.of() : List.copyOf(facilities);
		this.publicVisible = publicVisible;
		this.instructions = instructions;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public void update(
		String name,
		Point location,
		Integer capacity,
		Integer currentOccupancy,
		SafeLocationStatus status,
		String contactNumber,
		List<String> facilities,
		Boolean publicVisible,
		String instructions
	) {
		if (name != null) {
			this.name = name;
		}
		if (location != null) {
			this.location = location;
		}
		if (capacity != null) {
			this.capacity = capacity;
		}
		if (currentOccupancy != null) {
			this.currentOccupancy = currentOccupancy;
		}
		if (status != null) {
			this.status = status;
		}
		if (contactNumber != null) {
			this.contactNumber = contactNumber;
		}
		if (facilities != null) {
			this.facilities = List.copyOf(facilities);
		}
		if (publicVisible != null) {
			this.publicVisible = publicVisible;
		}
		if (instructions != null) {
			this.instructions = instructions;
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

	public Point getLocation() {
		return location;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public Integer getCurrentOccupancy() {
		return currentOccupancy;
	}

	public SafeLocationStatus getStatus() {
		return status;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public List<String> getFacilities() {
		return facilities == null ? List.of() : facilities;
	}

	public boolean isPublicVisible() {
		return publicVisible;
	}

	public String getInstructions() {
		return instructions;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
