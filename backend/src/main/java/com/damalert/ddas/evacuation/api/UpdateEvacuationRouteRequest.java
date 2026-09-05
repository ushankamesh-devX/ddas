package com.damalert.ddas.evacuation.api;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.damalert.ddas.common.geo.GeoJsonLineString;
import com.damalert.ddas.evacuation.domain.RouteStatus;

/**
 * Partial update. A null field leaves the stored value unchanged.
 * Setting {@code routeStatus} to BLOCKED or CLOSED is how a route is taken out of service.
 */
public record UpdateEvacuationRouteRequest(
	@Size(max = 180) String name,
	UUID fromZoneId,
	UUID safeLocationId,
	@Valid GeoJsonLineString geometry,
	RouteStatus routeStatus,
	Boolean publicVisible,
	String instructions,
	@Min(1) Short priority
) {
}
