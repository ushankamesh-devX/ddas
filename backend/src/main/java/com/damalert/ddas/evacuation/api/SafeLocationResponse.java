package com.damalert.ddas.evacuation.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.domain.SafeLocation;
import com.damalert.ddas.evacuation.domain.SafeLocationStatus;

public record SafeLocationResponse(
	UUID id,
	UUID damId,
	String code,
	String name,
	GeoJsonPoint location,
	Integer capacity,
	Integer currentOccupancy,
	SafeLocationStatus status,
	String contactNumber,
	List<String> facilities,
	boolean publicVisible,
	String instructions,
	Instant createdAt,
	Instant updatedAt
) {
	public static SafeLocationResponse from(SafeLocation safeLocation, GeometryMapper geometryMapper) {
		return new SafeLocationResponse(
			safeLocation.getId(),
			safeLocation.getDamId(),
			safeLocation.getCode(),
			safeLocation.getName(),
			geometryMapper.toGeoJson(safeLocation.getLocation()),
			safeLocation.getCapacity(),
			safeLocation.getCurrentOccupancy(),
			safeLocation.getStatus(),
			safeLocation.getContactNumber(),
			safeLocation.getFacilities(),
			safeLocation.isPublicVisible(),
			safeLocation.getInstructions(),
			safeLocation.getCreatedAt(),
			safeLocation.getUpdatedAt()
		);
	}
}
