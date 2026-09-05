package com.damalert.ddas.evacuation.api;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.damalert.ddas.common.geo.GeoJsonLineString;
import com.damalert.ddas.evacuation.domain.RouteStatus;

public record CreateEvacuationRouteRequest(
	@NotBlank @Size(max = 100) String code,
	@NotBlank @Size(max = 180) String name,
	@NotNull UUID fromZoneId,
	@NotNull UUID safeLocationId,
	@NotNull @Valid GeoJsonLineString geometry,
	RouteStatus routeStatus,
	Boolean publicVisible,
	String instructions,
	@Min(1) Short priority
) {
	public boolean publicVisibleOrDefault() {
		return publicVisible == null || publicVisible;
	}
}
