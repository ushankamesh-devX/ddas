package com.damalert.ddas.evacuation.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.evacuation.domain.RiskZoneSeverity;

public record CreateRiskZoneRequest(
	@NotBlank @Size(max = 100) String code,
	@NotBlank @Size(max = 180) String name,
	@NotNull RiskZoneSeverity severity,
	@NotNull @Valid GeoJsonPolygon geometry,
	Boolean evacuationRequired,
	Boolean publicVisible,
	String instructions
) {
	public boolean evacuationRequiredOrDefault() {
		return evacuationRequired != null && evacuationRequired;
	}

	public boolean publicVisibleOrDefault() {
		return publicVisible == null || publicVisible;
	}
}
