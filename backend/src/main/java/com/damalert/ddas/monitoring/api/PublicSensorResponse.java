package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;

public record PublicSensorResponse(
	UUID id,
	String name,
	String sensorType,
	String unit,
	SensorStatus status,
	Instant lastSeenAt,
	GeoJsonPoint location,
	BigDecimal latestValue,
	Instant measuredAt,
	String quality
) {
	public static PublicSensorResponse from(Sensor sensor, SensorStatus effectiveStatus, LatestReadingResponse latest) {
		GeoJsonPoint point = null;
		if (sensor.getVisibility() == SensorVisibility.PUBLIC && sensor.isExposeExactLocation()
			&& sensor.getLocation() != null) {
			point = new GeoJsonPoint("Point", List.of(sensor.getLocation().getX(), sensor.getLocation().getY()));
		}
		return new PublicSensorResponse(sensor.getId(), sensor.getName(), sensor.getSensorType(), sensor.getUnit(),
			effectiveStatus, sensor.getLastSeenAt(), point, latest == null ? null : latest.value(),
			latest == null ? null : latest.measuredAt(), latest == null ? null : latest.quality());
	}
}
