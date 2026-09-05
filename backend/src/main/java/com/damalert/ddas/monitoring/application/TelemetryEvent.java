package com.damalert.ddas.monitoring.application;

import java.util.UUID;

import com.damalert.ddas.monitoring.api.LatestReadingResponse;

public record TelemetryEvent(UUID damId, LatestReadingResponse reading) {
}
