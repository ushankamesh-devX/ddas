package com.damalert.ddas.evacuation.api;

import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonLineString;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.domain.EvacuationRoute;
import com.damalert.ddas.evacuation.domain.RouteStatus;

/**
 * Public projection of an evacuation route.
 *
 * <p>A blocked or closed route is still returned so that the map can show it as unusable,
 * but {@code recommended} is false for it. Removing it outright would leave a civilian who
 * cached the route earlier with no signal that it has since been closed.
 */
public record PublicEvacuationRouteResponse(
	UUID id,
	String code,
	String name,
	UUID fromZoneId,
	UUID safeLocationId,
	GeoJsonLineString geometry,
	RouteStatus routeStatus,
	boolean recommended,
	short priority,
	String instructions
) {
	public static PublicEvacuationRouteResponse from(EvacuationRoute route, GeometryMapper geometryMapper) {
		return new PublicEvacuationRouteResponse(
			route.getId(),
			route.getCode(),
			route.getName(),
			route.getFromZoneId(),
			route.getSafeLocationId(),
			geometryMapper.toGeoJson(route.getGeometry()),
			route.getRouteStatus(),
			route.getRouteStatus() == RouteStatus.ACTIVE,
			route.getPriority(),
			route.getInstructions()
		);
	}
}
