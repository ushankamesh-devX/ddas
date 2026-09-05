package com.damalert.ddas.monitoring.api;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.monitoring.application.SensorService;
import com.damalert.ddas.monitoring.application.TelemetryStreamService;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/telemetry")
public class TelemetryController {

	private final SensorService sensors;
	private final TelemetryStreamService streams;
	private final DamAccessChecker accessChecker;
	private final CurrentUserProvider currentUser;

	public TelemetryController(SensorService sensors, TelemetryStreamService streams,
		DamAccessChecker accessChecker, CurrentUserProvider currentUser) {
		this.sensors = sensors;
		this.streams = streams;
		this.accessChecker = accessChecker;
		this.currentUser = currentUser;
	}

	@GetMapping("/latest")
	List<LatestReadingResponse> latest(@PathVariable UUID damId) {
		return sensors.latest(currentUser.requireCurrentUser(), damId);
	}

	@GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	SseEmitter stream(@PathVariable UUID damId) {
		CurrentUser user = currentUser.requireCurrentUser();
		accessChecker.requireStaffAccess(user, damId);
		return streams.subscribe(damId);
	}
}
