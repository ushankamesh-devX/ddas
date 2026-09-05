package com.damalert.ddas.evacuation.api;

import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.domain.RiskZone;
import com.damalert.ddas.evacuation.domain.RiskZoneSeverity;

/**
 * Public projection of a risk zone. Deliberately separate from {@link RiskZoneResponse}
 * so that private or operational fields cannot leak into the public feed by accident.
 */
public record PublicRiskZoneResponse(
	UUID id,
	String code,
	String name,
	RiskZoneSeverity severity,
	GeoJsonPolygon geometry,
	boolean evacuationRequired,
	String instructions
) {
	public static PublicRiskZoneResponse from(RiskZone zone, GeometryMapper geometryMapper) {
		return new PublicRiskZoneResponse(
			zone.getId(),
			zone.getCode(),
			zone.getName(),
			zone.getSeverity(),
			geometryMapper.toGeoJson(zone.getGeometry()),
			zone.isEvacuationRequired(),
			zone.getInstructions()
		);
	}
}
