package com.damalert.ddas.monitoring.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "iot_device_sensor")
public class IotDeviceSensor {

	@EmbeddedId
	private IotDeviceSensorId id;

	@Column(name = "assigned_at", nullable = false)
	private Instant assignedAt;

	protected IotDeviceSensor() {
	}

	public IotDeviceSensor(IotDeviceSensorId id) {
		this.id = id;
		this.assignedAt = Instant.now();
	}

	public IotDeviceSensorId getId() { return id; }
}
