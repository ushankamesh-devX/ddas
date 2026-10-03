package com.damalert.alert.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.alert.dto.CreateAlertDto;
import com.damalert.alert.entity.Alert;
import com.damalert.alert.service.AlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AlertController {

	private final AlertService alertService;

	@PostMapping("/dams/{damId}/alerts")
	public ResponseEntity<Alert> createAlert(
		@PathVariable UUID damId,
		@RequestBody CreateAlertDto request
	) {
		UUID currentUserId = UUID.randomUUID(); // TODO: extract from Spring Security Context
		Alert created = alertService.createAlert(request, currentUserId);
		return ResponseEntity.created(URI.create("/api/v1/alerts/" + created.getId())).body(created);
	}

	@GetMapping("/dams/{damId}/alerts")
	public ResponseEntity<List<Alert>> listAlerts(@PathVariable UUID damId) {
		return ResponseEntity.ok(alertService.listAlerts(damId));
	}

	@PostMapping("/alerts/{alertId}/cancel")
	public ResponseEntity<Alert> cancelAlert(@PathVariable UUID alertId) {
		UUID currentUserId = UUID.randomUUID(); // TODO: extract from Spring Security Context
		return ResponseEntity.ok(alertService.cancelAlert(alertId, currentUserId));
	}

	@PostMapping("/alerts/{alertId}/acknowledge")
	public ResponseEntity<Void> acknowledgeAlert(@PathVariable UUID alertId) {
		UUID currentUserId = UUID.randomUUID(); // TODO: extract from Spring Security Context
		alertService.acknowledgeAlert(alertId, currentUserId);
		return ResponseEntity.ok().build();
	}
}