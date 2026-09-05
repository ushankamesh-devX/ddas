package com.damalert.ddas.evacuation.api;

import java.util.List;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.domain.SafeLocation;
import com.damalert.ddas.evacuation.domain.SafeLocationStatus;

/**
 * Public projection of a safe location. {@code currentOccupancy} is intentionally omitted:
 * shelter occupancy is operational data, while capacity and status are what the public needs.
 */
public record PublicSafeLocationResponse(
	UUID id,
	String code,
	String name,
	GeoJsonPoint location,
	Integer capacity,
	SafeLocationStatus status,
	boolean acceptingPeople,
	String contactNumber,
	List<String> facilities,
	String instructions
) {
	public static PublicSafeLocationResponse from(SafeLocation safeLocation, GeometryMapper geometryMapper) {
		return new PublicSafeLocationResponse(
			safeLocation.getId(),
			safeLocation.getCode(),
			safeLocation.getName(),
			geometryMapper.toGeoJson(safeLocation.getLocation()),
			safeLocation.getCapacity(),
			safeLocation.getStatus(),
			safeLocation.getStatus() != SafeLocationStatus.CLOSED
				&& safeLocation.getStatus() != SafeLocationStatus.FULL,
			safeLocation.getContactNumber(),
			safeLocation.getFacilities(),
			safeLocation.getInstructions()
		);
	}
}
