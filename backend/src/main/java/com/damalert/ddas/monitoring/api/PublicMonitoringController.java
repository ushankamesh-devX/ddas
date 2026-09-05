package com.damalert.ddas.monitoring.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.monitoring.application.PublicDamService;
import com.damalert.ddas.monitoring.application.SensorService;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/public/dams")
public class PublicMonitoringController {

	private final SensorService sensors;
	private final PublicDamService dams;

	public PublicMonitoringController(SensorService sensors, PublicDamService dams) {
		this.sensors = sensors;
		this.dams = dams;
	}

	@GetMapping
	List<PublicDamResponse> dams() {
		return dams.list();
	}

	@GetMapping("/{damId}")
	PublicDamResponse dam(@PathVariable UUID damId) {
		return dams.get(damId);
	}

	@GetMapping("/{damId}/sensors")
	List<PublicSensorResponse> sensors(@PathVariable UUID damId) {
		dams.get(damId);
		return sensors.listPublic(damId);
	}

	@GetMapping("/{damId}/sensors/{sensorId}/readings")
	List<ReadingResponse> history(@PathVariable UUID damId, @PathVariable UUID sensorId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
		@RequestParam(defaultValue = "100") int size) {
		dams.get(damId);
		return sensors.publicHistory(damId, sensorId, from, to, size);
	}

	@GetMapping("/{damId}/map")
	PublicDamMapResponse map(@PathVariable UUID damId) {
		return dams.map(damId);
	}
}
