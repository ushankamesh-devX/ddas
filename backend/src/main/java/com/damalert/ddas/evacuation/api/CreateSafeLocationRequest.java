package com.damalert.ddas.evacuation.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.evacuation.domain.SafeLocationStatus;

public record CreateSafeLocationRequest(
	@NotBlank @Size(max = 100) String code,
	@NotBlank @Size(max = 180) String name,
	@NotNull @Valid GeoJsonPoint location,
	// Validated here rather than relying on the database CHECK, which would surface as a 500.
	@Min(0) Integer capacity,
	@Min(0) Integer currentOccupancy,
	SafeLocationStatus status,
	@Size(max = 32) String contactNumber,
	List<String> facilities,
	Boolean publicVisible,
	String instructions
) {
	public boolean publicVisibleOrDefault() {
		return publicVisible == null || publicVisible;
	}
}
