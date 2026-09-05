package com.damalert.ddas.monitoring.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.monitoring.application.IotDeviceService;
import com.damalert.ddas.monitoring.domain.DeviceStatus;

import jakarta.validation.Valid;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/iot-devices")
public class IotDeviceController {
	private final IotDeviceService service;
	private final CurrentUserProvider currentUser;

	public IotDeviceController(IotDeviceService service, CurrentUserProvider currentUser) {
		this.service = service;
		this.currentUser = currentUser;
	}

	@GetMapping
	List<IotDeviceResponse> list(@PathVariable UUID damId) {
		return service.list(currentUser.requireCurrentUser(), damId);
	}

	@PostMapping
	ResponseEntity<IotDeviceResponse> create(@PathVariable UUID damId,
		@Valid @RequestBody CreateIotDeviceRequest request) {
		IotDeviceResponse created = service.create(currentUser.requireCurrentUser(), damId, request);
		return ResponseEntity.created(URI.create("/api/v1/dams/" + damId + "/iot-devices/" + created.id())).body(created);
	}

	@GetMapping("/{deviceId}")
	IotDeviceResponse get(@PathVariable UUID damId, @PathVariable UUID deviceId) {
		return service.get(currentUser.requireCurrentUser(), damId, deviceId);
	}

	@PatchMapping("/{deviceId}")
	IotDeviceResponse update(@PathVariable UUID damId, @PathVariable UUID deviceId,
		@Valid @RequestBody UpdateIotDeviceRequest request) {
		return service.update(currentUser.requireCurrentUser(), damId, deviceId, request);
	}

	@PostMapping("/{deviceId}/rotate-key")
	IotDeviceResponse rotate(@PathVariable UUID damId, @PathVariable UUID deviceId) {
		return service.rotate(currentUser.requireCurrentUser(), damId, deviceId);
	}

	@PostMapping("/{deviceId}/revoke")
	ResponseEntity<Void> revoke(@PathVariable UUID damId, @PathVariable UUID deviceId,
		@Valid @RequestBody(required = false) DeviceActionRequest request) {
		service.setStatus(currentUser.requireCurrentUser(), damId, deviceId, DeviceStatus.REVOKED,
			request == null ? null : request.reason());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{deviceId}/disable")
	ResponseEntity<Void> disable(@PathVariable UUID damId, @PathVariable UUID deviceId,
		@Valid @RequestBody(required = false) DeviceActionRequest request) {
		service.setStatus(currentUser.requireCurrentUser(), damId, deviceId, DeviceStatus.DISABLED,
			request == null ? null : request.reason());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{deviceId}/enable")
	ResponseEntity<Void> enable(@PathVariable UUID damId, @PathVariable UUID deviceId) {
		service.setStatus(currentUser.requireCurrentUser(), damId, deviceId, DeviceStatus.ACTIVE, null);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{deviceId}/sensors/{sensorId}")
	ResponseEntity<Void> assign(@PathVariable UUID damId, @PathVariable UUID deviceId,
		@PathVariable UUID sensorId) {
		service.assign(currentUser.requireCurrentUser(), damId, deviceId, sensorId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{deviceId}/sensors/{sensorId}")
	ResponseEntity<Void> unassign(@PathVariable UUID damId, @PathVariable UUID deviceId,
		@PathVariable UUID sensorId) {
		service.unassign(currentUser.requireCurrentUser(), damId, deviceId, sensorId);
		return ResponseEntity.noContent().build();
	}
}
