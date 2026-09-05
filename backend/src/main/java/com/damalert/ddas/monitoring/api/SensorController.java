package com.damalert.ddas.monitoring.api;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.monitoring.application.SensorService;

import jakarta.validation.Valid;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/sensors")
public class SensorController {

	private final SensorService service;
	private final CurrentUserProvider currentUser;

	public SensorController(SensorService service, CurrentUserProvider currentUser) {
		this.service = service;
		this.currentUser = currentUser;
	}

	@GetMapping
	List<SensorResponse> list(@PathVariable UUID damId) {
		return service.list(currentUser.requireCurrentUser(), damId);
	}

	@PostMapping
	ResponseEntity<SensorResponse> create(@PathVariable UUID damId, @Valid @RequestBody CreateSensorRequest request) {
		SensorResponse created = service.create(currentUser.requireCurrentUser(), damId, request);
		return ResponseEntity.created(URI.create("/api/v1/dams/" + damId + "/sensors/" + created.id())).body(created);
	}

	@GetMapping("/{sensorId}")
	SensorResponse get(@PathVariable UUID damId, @PathVariable UUID sensorId) {
		return service.get(currentUser.requireCurrentUser(), damId, sensorId);
	}

	@PutMapping("/{sensorId}")
	SensorResponse update(@PathVariable UUID damId, @PathVariable UUID sensorId,
		@Valid @RequestBody UpdateSensorRequest request) {
		return service.update(currentUser.requireCurrentUser(), damId, sensorId, request);
	}

	@DeleteMapping("/{sensorId}")
	ResponseEntity<Void> delete(@PathVariable UUID damId, @PathVariable UUID sensorId) {
		service.delete(currentUser.requireCurrentUser(), damId, sensorId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{sensorId}/readings")
	List<ReadingResponse> history(@PathVariable UUID damId, @PathVariable UUID sensorId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
		@RequestParam(defaultValue = "200") int size) {
		return service.history(currentUser.requireCurrentUser(), damId, sensorId, from, to, size);
	}
}
