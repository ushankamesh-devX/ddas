package com.damalert.ddas.monitoring.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.damalert.ddas.monitoring.domain.DeviceAuthMethod;
import com.damalert.ddas.monitoring.domain.DeviceStatus;
import com.damalert.ddas.monitoring.domain.DeviceType;
import com.damalert.ddas.monitoring.domain.IotDevice;

public record IotDeviceResponse(
	UUID id,
	UUID damId,
	String deviceId,
	String name,
	DeviceType deviceType,
	DeviceAuthMethod authMethod,
	DeviceStatus status,
	String firmwareVersion,
	Instant lastConnectedAt,
	Instant revokedAt,
	Instant createdAt,
	List<UUID> sensorIds,
	DeviceCredentialsResponse credentials,
	MqttConnectionResponse mqtt
) {
	public static IotDeviceResponse safe(IotDevice device, List<UUID> sensorIds) {
		return new IotDeviceResponse(device.getId(), device.getDamId(), device.getDeviceUid(), device.getName(),
			device.getDeviceType(), device.getAuthMethod(), device.getStatus(), device.getFirmwareVersion(),
			device.getLastConnectedAt(), device.getRevokedAt(), device.getCreatedAt(), sensorIds, null, null);
	}

	public static IotDeviceResponse withOneTimeKey(IotDevice device, List<UUID> sensorIds, String key,
		MqttConnectionResponse mqtt) {
		return new IotDeviceResponse(device.getId(), device.getDamId(), device.getDeviceUid(), device.getName(),
			device.getDeviceType(), device.getAuthMethod(), device.getStatus(), device.getFirmwareVersion(),
			device.getLastConnectedAt(), device.getRevokedAt(), device.getCreatedAt(), sensorIds,
			new DeviceCredentialsResponse(device.getDeviceUid(), key, true), mqtt);
	}
}
