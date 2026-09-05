package com.damalert.ddas.monitoring.application;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.monitoring.api.CreateIotDeviceRequest;
import com.damalert.ddas.monitoring.api.IotDeviceResponse;
import com.damalert.ddas.monitoring.api.MqttConnectionResponse;
import com.damalert.ddas.monitoring.api.UpdateIotDeviceRequest;
import com.damalert.ddas.monitoring.domain.CredentialStatus;
import com.damalert.ddas.monitoring.domain.DeviceAuthMethod;
import com.damalert.ddas.monitoring.domain.DeviceStatus;
import com.damalert.ddas.monitoring.domain.IotDevice;
import com.damalert.ddas.monitoring.domain.IotDeviceCredential;
import com.damalert.ddas.monitoring.domain.IotDeviceSensor;
import com.damalert.ddas.monitoring.domain.IotDeviceSensorId;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.persistence.IotDeviceCredentialRepository;
import com.damalert.ddas.monitoring.persistence.IotDeviceRepository;
import com.damalert.ddas.monitoring.persistence.IotDeviceSensorRepository;

@Service
@Profile("!standalone")
@Transactional
public class IotDeviceService {
	private final IotDeviceRepository devices;
	private final IotDeviceCredentialRepository credentials;
	private final IotDeviceSensorRepository assignments;
	private final SensorService sensors;
	private final DamReader dams;
	private final DamAccessChecker access;
	private final AuditService audit;
	private final DeviceKeyService keys;
	private final BrokerCredentialProvisioner broker;
	private final String brokerUri;

	public IotDeviceService(IotDeviceRepository devices, IotDeviceCredentialRepository credentials,
		IotDeviceSensorRepository assignments, SensorService sensors, DamReader dams, DamAccessChecker access,
		AuditService audit, DeviceKeyService keys, BrokerCredentialProvisioner broker,
		@Value("${app.mqtt.broker-uri:tcp://localhost:1883}") String brokerUri) {
		this.devices = devices;
		this.credentials = credentials;
		this.assignments = assignments;
		this.sensors = sensors;
		this.dams = dams;
		this.access = access;
		this.audit = audit;
		this.keys = keys;
		this.broker = broker;
		this.brokerUri = brokerUri;
	}

	@Transactional(readOnly = true)
	public List<IotDeviceResponse> list(CurrentUser user, UUID damId) {
		access.requireStaffAccess(user, damId);
		return devices.findAllByDamIdOrderByNameAsc(damId).stream().map(this::safe).toList();
	}

	@Transactional(readOnly = true)
	public IotDeviceResponse get(CurrentUser user, UUID damId, UUID deviceId) {
		access.requireStaffAccess(user, damId);
		return safe(require(damId, deviceId));
	}

	public IotDeviceResponse create(CurrentUser user, UUID damId, CreateIotDeviceRequest request) {
		requireManage(user, damId);
		dams.requireDam(damId);
		if (request.authMethod() != DeviceAuthMethod.DEVICE_KEY) {
			throw new com.damalert.ddas.common.error.BadRequestException("UNSUPPORTED_DEVICE_AUTH",
				"V1 device provisioning supports DEVICE_KEY authentication.");
		}
		IotDevice device = devices.saveAndFlush(new IotDevice(UUID.randomUUID(), damId, request.name().trim(),
			request.deviceType(), request.authMethod()));
		DeviceKeyService.GeneratedDeviceKey key = keys.generate();
		String brokerRef = broker.provision(device.getDeviceUid(), key.plaintext());
		credentials.save(new IotDeviceCredential(device.getId(), key.prefix(), key.fingerprint(), key.verifier(), brokerRef));
		record(user, damId, "IOT_DEVICE_CREATED", device.getId(), Map.of("deviceId", device.getDeviceUid()));
		return IotDeviceResponse.withOneTimeKey(device, List.of(), key.plaintext(), mqtt(device));
	}

	public IotDeviceResponse update(CurrentUser user, UUID damId, UUID deviceId, UpdateIotDeviceRequest request) {
		requireManage(user, damId);
		IotDevice device = require(damId, deviceId);
		try {
			device.update(request.name().trim(), request.deviceType(), request.firmwareVersion());
		}
		catch (IllegalStateException ex) {
			throw new ConflictException("DEVICE_REVOKED", ex.getMessage());
		}
		record(user, damId, "IOT_DEVICE_UPDATED", deviceId, Map.of());
		return safe(device);
	}

	public IotDeviceResponse rotate(CurrentUser user, UUID damId, UUID deviceId) {
		requireManage(user, damId);
		IotDevice device = requireActive(damId, deviceId);
		IotDeviceCredential old = activeCredential(deviceId);
		DeviceKeyService.GeneratedDeviceKey key = keys.generate();
		broker.rotate(old.getBrokerCredentialRef(), device.getDeviceUid(), key.plaintext());
		old.rotate();
		credentials.saveAndFlush(old);
		credentials.save(new IotDeviceCredential(deviceId, key.prefix(), key.fingerprint(), key.verifier(),
			old.getBrokerCredentialRef()));
		record(user, damId, "IOT_DEVICE_KEY_ROTATED", deviceId, Map.of("keyPrefix", key.prefix()));
		return IotDeviceResponse.withOneTimeKey(device, sensorIds(deviceId), key.plaintext(), mqtt(device));
	}

	public void setStatus(CurrentUser user, UUID damId, UUID deviceId, DeviceStatus status, String reason) {
		requireManage(user, damId);
		IotDevice device = require(damId, deviceId);
		if (status == DeviceStatus.REVOKED) {
			IotDeviceCredential credential = activeCredential(deviceId);
			broker.revoke(credential.getBrokerCredentialRef());
			credential.revoke();
		}
		else if (status == DeviceStatus.DISABLED) {
			broker.disable(activeCredential(deviceId).getBrokerCredentialRef());
		}
		else if (device.getStatus() == DeviceStatus.DISABLED) {
			broker.enable(activeCredential(deviceId).getBrokerCredentialRef(), device.getDeviceUid());
		}
		try {
			device.setStatus(status, user.userId());
		}
		catch (IllegalStateException ex) {
			throw new ConflictException("DEVICE_REVOKED", ex.getMessage());
		}
		record(user, damId, "IOT_DEVICE_" + status.name(), deviceId,
			reason == null ? Map.of() : Map.of("reason", reason));
	}

	public void assign(CurrentUser user, UUID damId, UUID deviceId, UUID sensorId) {
		requireManage(user, damId);
		IotDevice device = requireActive(damId, deviceId);
		Sensor sensor = sensors.requireSensor(damId, sensorId);
		assignments.findByIdSensorId(sensorId).ifPresent(existing -> {
			if (!existing.getId().deviceId().equals(deviceId)) {
				throw new ConflictException("SENSOR_ALREADY_ASSIGNED", "Sensor is already assigned to another device.");
			}
		});
		assignments.save(new IotDeviceSensor(new IotDeviceSensorId(device.getId(), sensor.getId())));
		record(user, damId, "IOT_SENSOR_ASSIGNED", deviceId, Map.of("sensorId", sensorId));
	}

	public void unassign(CurrentUser user, UUID damId, UUID deviceId, UUID sensorId) {
		requireManage(user, damId);
		require(damId, deviceId);
		sensors.requireSensor(damId, sensorId);
		if (!assignments.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)) {
			throw new NotFoundException("DEVICE_SENSOR_ASSIGNMENT_NOT_FOUND", "Sensor is not assigned to this device.");
		}
		assignments.deleteByIdDeviceIdAndIdSensorId(deviceId, sensorId);
		record(user, damId, "IOT_SENSOR_UNASSIGNED", deviceId, Map.of("sensorId", sensorId));
	}

	private IotDeviceResponse safe(IotDevice device) {
		return IotDeviceResponse.safe(device, sensorIds(device.getId()));
	}

	private List<UUID> sensorIds(UUID deviceId) {
		return assignments.findAllByIdDeviceId(deviceId).stream().map(a -> a.getId().sensorId()).toList();
	}

	private IotDevice require(UUID damId, UUID deviceId) {
		return devices.findByIdAndDamId(deviceId, damId)
			.orElseThrow(() -> new NotFoundException("IOT_DEVICE_NOT_FOUND", "IoT device does not exist."));
	}

	private IotDevice requireActive(UUID damId, UUID deviceId) {
		IotDevice device = require(damId, deviceId);
		if (device.getStatus() != DeviceStatus.ACTIVE) {
			throw new ConflictException("IOT_DEVICE_NOT_ACTIVE", "IoT device is not active.");
		}
		return device;
	}

	private IotDeviceCredential activeCredential(UUID deviceId) {
		return credentials.findByDeviceIdAndStatus(deviceId, CredentialStatus.ACTIVE)
			.orElseThrow(() -> new ConflictException("ACTIVE_DEVICE_CREDENTIAL_MISSING", "Device has no active credential."));
	}

	private MqttConnectionResponse mqtt(IotDevice device) {
		URI uri = URI.create(brokerUri);
		int port = uri.getPort() > 0 ? uri.getPort() : ("ssl".equals(uri.getScheme()) ? 8883 : 1883);
		return new MqttConnectionResponse(uri.getHost(), port,
			"dams/" + device.getDamId() + "/devices/" + device.getDeviceUid() + "/telemetry");
	}

	private void requireManage(CurrentUser user, UUID damId) {
		access.requireRole(user, damId, DamStaffRole.DAM_ADMIN, DamStaffRole.DAM_ENGINEER);
	}

	private void record(CurrentUser user, UUID damId, String action, UUID id, Map<String, Object> value) {
		audit.record(new AuditEvent(damId, user.userId(), action, "iot_device", id, null, value));
	}
}
