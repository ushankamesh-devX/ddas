package com.damalert.ddas.evacuation.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.evacuation.domain.RiskZoneSeverity;

/**
 * Partial update. A null field leaves the stored value unchanged.
 */
public record UpdateRiskZoneRequest(
	@Size(max = 180) String name,
	RiskZoneSeverity severity,
	@Valid GeoJsonPolygon geometry,
	Boolean evacuationRequired,
	Boolean publicVisible,
	String instructions,
	Boolean active
) {
}
