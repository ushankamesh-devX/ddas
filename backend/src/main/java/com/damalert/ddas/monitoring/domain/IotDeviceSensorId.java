package com.damalert.ddas.monitoring.domain;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record IotDeviceSensorId(
	@Column(name = "device_id") UUID deviceId,
	@Column(name = "sensor_id") UUID sensorId
) implements Serializable {
}
