package com.damalert.ddas.evacuation.api;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.evacuation.application.EmergencyService;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/emergency")
public class EmergencyController {

	private final EmergencyService emergencyService;
	private final CurrentUserProvider currentUserProvider;

	public EmergencyController(EmergencyService emergencyService, CurrentUserProvider currentUserProvider) {
		this.emergencyService = emergencyService;
		this.currentUserProvider = currentUserProvider;
	}

	@GetMapping
	EmergencyStateResponse get(@PathVariable UUID damId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return EmergencyStateResponse.from(emergencyService.get(user, damId));
	}

	@PostMapping("/activate")
	EmergencyStateResponse activate(
		@PathVariable UUID damId,
		@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
		@Valid @RequestBody(required = false) EmergencyActionRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return EmergencyStateResponse.from(
			emergencyService.activate(user, damId, reasonOf(request), idempotencyKey)
		);
	}

	@PostMapping("/clear")
	EmergencyStateResponse clear(
		@PathVariable UUID damId,
		@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
		@Valid @RequestBody(required = false) EmergencyActionRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return EmergencyStateResponse.from(
			emergencyService.clear(user, damId, reasonOf(request), idempotencyKey)
		);
	}

	private String reasonOf(EmergencyActionRequest request) {
		return request == null ? null : request.reason();
	}
}
