package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorReading;
import com.damalert.ddas.monitoring.domain.SensorStatus;

public record LatestReadingResponse(
	UUID sensorId,
	String sensorName,
	String sensorType,
	String unit,
	SensorStatus status,
	BigDecimal value,
	String quality,
	Instant measuredAt,
	Instant receivedAt
) {
	public static LatestReadingResponse from(Sensor sensor, SensorStatus status, SensorReading reading) {
		return new LatestReadingResponse(sensor.getId(), sensor.getName(), sensor.getSensorType(), sensor.getUnit(),
			status, reading == null ? null : reading.getValue(), reading == null ? null : reading.getQuality().name(),
			reading == null ? null : reading.getMeasuredAt(), reading == null ? null : reading.getReceivedAt());
	}
}
