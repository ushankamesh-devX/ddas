package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.damalert.ddas.monitoring.domain.SensorReading;

public record ReadingResponse(
	long id,
	UUID sensorId,
	Instant measuredAt,
	Instant receivedAt,
	BigDecimal value,
	String quality,
	String messageId
) {
	public static ReadingResponse from(SensorReading reading) {
		return new ReadingResponse(reading.getId(), reading.getSensorId(), reading.getMeasuredAt(),
			reading.getReceivedAt(), reading.getValue(), reading.getQuality().name(), reading.getExternalMessageId());
	}
}
