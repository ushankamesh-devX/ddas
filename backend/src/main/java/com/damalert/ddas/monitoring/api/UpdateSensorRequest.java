package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.SensorStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.domain.ThresholdDirection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSensorRequest(
	@NotBlank @Size(max = 180) String name,
	@NotBlank @Size(max = 48) String sensorType,
	@NotBlank @Size(max = 32) String unit,
	@Valid GeoJsonPoint location,
	@NotNull SensorVisibility visibility,
	boolean exposeExactLocation,
	BigDecimal warningThreshold,
	BigDecimal criticalThreshold,
	@NotNull ThresholdDirection thresholdDirection,
	@NotNull SensorStatus status
) {
}
