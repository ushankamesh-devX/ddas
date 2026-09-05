package com.damalert.ddas.evacuation.api;

import java.time.Instant;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonLineString;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.domain.EvacuationRoute;
import com.damalert.ddas.evacuation.domain.RouteStatus;

public record EvacuationRouteResponse(
	UUID id,
	UUID damId,
	String code,
	String name,
	UUID fromZoneId,
	UUID safeLocationId,
	GeoJsonLineString geometry,
	RouteStatus routeStatus,
	boolean publicVisible,
	String instructions,
	short priority,
	Instant createdAt,
	Instant updatedAt
) {
	public static EvacuationRouteResponse from(EvacuationRoute route, GeometryMapper geometryMapper) {
		return new EvacuationRouteResponse(
			route.getId(),
			route.getDamId(),
			route.getCode(),
			route.getName(),
			route.getFromZoneId(),
			route.getSafeLocationId(),
			geometryMapper.toGeoJson(route.getGeometry()),
			route.getRouteStatus(),
			route.isPublicVisible(),
			route.getInstructions(),
			route.getPriority(),
			route.getCreatedAt(),
			route.getUpdatedAt()
		);
	}
}
