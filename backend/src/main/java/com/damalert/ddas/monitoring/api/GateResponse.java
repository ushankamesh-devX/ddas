package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.DamGate;
import com.damalert.ddas.monitoring.domain.GateStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;

public record GateResponse(
	UUID id,
	UUID damId,
	String code,
	String name,
	GeoJsonPoint location,
	GateStatus status,
	BigDecimal openingPercent,
	SensorVisibility visibility,
	Instant updatedAt
) {
	public static GateResponse from(DamGate gate) {
		GeoJsonPoint point = gate.getLocation() == null ? null : new GeoJsonPoint(
			"Point", List.of(gate.getLocation().getX(), gate.getLocation().getY()));
		return new GateResponse(gate.getId(), gate.getDamId(), gate.getCode(), gate.getName(), point,
			gate.getStatus(), gate.getOpeningPercent(), gate.getVisibility(), gate.getUpdatedAt());
	}
}
