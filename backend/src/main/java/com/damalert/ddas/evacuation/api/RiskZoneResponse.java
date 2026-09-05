package com.damalert.ddas.evacuation.api;

import java.time.Instant;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.domain.RiskZone;
import com.damalert.ddas.evacuation.domain.RiskZoneSeverity;

public record RiskZoneResponse(
	UUID id,
	UUID damId,
	String code,
	String name,
	RiskZoneSeverity severity,
	GeoJsonPolygon geometry,
	boolean evacuationRequired,
	boolean publicVisible,
	String instructions,
	boolean active,
	Instant createdAt,
	Instant updatedAt
) {
	public static RiskZoneResponse from(RiskZone zone, GeometryMapper geometryMapper) {
		return new RiskZoneResponse(
			zone.getId(),
			zone.getDamId(),
			zone.getCode(),
			zone.getName(),
			zone.getSeverity(),
			geometryMapper.toGeoJson(zone.getGeometry()),
			zone.isEvacuationRequired(),
			zone.isPublicVisible(),
			zone.getInstructions(),
			zone.isActive(),
			zone.getCreatedAt(),
			zone.getUpdatedAt()
		);
	}
}
