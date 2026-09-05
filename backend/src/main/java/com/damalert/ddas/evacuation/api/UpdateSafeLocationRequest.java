package com.damalert.ddas.evacuation.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.evacuation.domain.SafeLocationStatus;

/**
 * Partial update. A null field leaves the stored value unchanged.
 */
public record UpdateSafeLocationRequest(
	@Size(max = 180) String name,
	@Valid GeoJsonPoint location,
	@Min(0) Integer capacity,
	@Min(0) Integer currentOccupancy,
	SafeLocationStatus status,
	@Size(max = 32) String contactNumber,
	List<String> facilities,
	Boolean publicVisible,
	String instructions
) {
}
