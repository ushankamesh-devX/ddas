package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.domain.ThresholdDirection;

public record SensorResponse(
	UUID id,
	UUID damId,
	String code,
	String name,
	String sensorType,
	String unit,
	GeoJsonPoint location,
	SensorVisibility visibility,
	boolean exposeExactLocation,
	BigDecimal warningThreshold,
	BigDecimal criticalThreshold,
	ThresholdDirection thresholdDirection,
	SensorStatus status,
	Instant lastSeenAt,
	Instant createdAt,
	Instant updatedAt
) {
	public static SensorResponse from(Sensor sensor) {
		GeoJsonPoint point = sensor.getLocation() == null ? null : new GeoJsonPoint(
			"Point", List.of(sensor.getLocation().getX(), sensor.getLocation().getY()));
		return new SensorResponse(sensor.getId(), sensor.getDamId(), sensor.getCode(), sensor.getName(),
			sensor.getSensorType(), sensor.getUnit(), point, sensor.getVisibility(), sensor.isExposeExactLocation(),
			sensor.getWarningThreshold(), sensor.getCriticalThreshold(), sensor.getThresholdDirection(),
			sensor.getStatus(), sensor.getLastSeenAt(), sensor.getCreatedAt(), sensor.getUpdatedAt());
	}
}
