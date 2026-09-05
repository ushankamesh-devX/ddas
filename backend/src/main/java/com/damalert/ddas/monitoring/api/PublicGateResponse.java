package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.DamGate;
import com.damalert.ddas.monitoring.domain.GateStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;

public record PublicGateResponse(UUID id, String name, GateStatus status, BigDecimal openingPercent, GeoJsonPoint location) {
	public static PublicGateResponse from(DamGate gate) {
		GeoJsonPoint point = gate.getVisibility() == SensorVisibility.PUBLIC && gate.getLocation() != null
			? new GeoJsonPoint("Point", List.of(gate.getLocation().getX(), gate.getLocation().getY())) : null;
		return new PublicGateResponse(gate.getId(), gate.getName(), gate.getStatus(), gate.getOpeningPercent(), point);
	}
}
