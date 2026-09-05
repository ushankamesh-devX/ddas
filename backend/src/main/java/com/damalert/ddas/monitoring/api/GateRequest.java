package com.damalert.ddas.monitoring.api;

import java.math.BigDecimal;

import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.monitoring.domain.GateStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GateRequest(
	@NotBlank @Size(max = 100) String code,
	@NotBlank @Size(max = 180) String name,
	@Valid GeoJsonPoint location,
	@NotNull GateStatus status,
	@DecimalMin("0") @DecimalMax("100") BigDecimal openingPercent,
	@NotNull SensorVisibility visibility
) {
}
