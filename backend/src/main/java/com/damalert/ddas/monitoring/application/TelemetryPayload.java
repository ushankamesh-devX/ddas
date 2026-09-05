package com.damalert.ddas.monitoring.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.monitoring.domain.ReadingQuality;

public record TelemetryPayload(String messageId, Instant measuredAt, List<TelemetryValue> readings) {
	public record TelemetryValue(UUID sensorId, BigDecimal value, ReadingQuality quality) { }
}
