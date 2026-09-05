package com.damalert.ddas.monitoring.api;

import com.damalert.ddas.monitoring.domain.DeviceAuthMethod;
import com.damalert.ddas.monitoring.domain.DeviceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIotDeviceRequest(
	@NotBlank @Size(max = 180) String name,
	@NotNull DeviceType deviceType,
	@NotNull DeviceAuthMethod authMethod
) {
}
