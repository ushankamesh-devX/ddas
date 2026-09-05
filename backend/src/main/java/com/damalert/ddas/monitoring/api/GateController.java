package com.damalert.ddas.monitoring.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.monitoring.application.GateService;

import jakarta.validation.Valid;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/gates")
public class GateController {
	private final GateService service;
	private final CurrentUserProvider currentUser;

	public GateController(GateService service, CurrentUserProvider currentUser) {
		this.service = service;
		this.currentUser = currentUser;
	}

	@GetMapping
	List<GateResponse> list(@PathVariable UUID damId) {
		return service.list(currentUser.requireCurrentUser(), damId);
	}

	@PostMapping
	ResponseEntity<GateResponse> create(@PathVariable UUID damId, @Valid @RequestBody GateRequest request) {
		GateResponse created = service.create(currentUser.requireCurrentUser(), damId, request);
		return ResponseEntity.created(URI.create("/api/v1/dams/" + damId + "/gates/" + created.id())).body(created);
	}

	@PutMapping("/{gateId}")
	GateResponse update(@PathVariable UUID damId, @PathVariable UUID gateId, @Valid @RequestBody GateRequest request) {
		return service.update(currentUser.requireCurrentUser(), damId, gateId, request);
	}

	@DeleteMapping("/{gateId}")
	ResponseEntity<Void> delete(@PathVariable UUID damId, @PathVariable UUID gateId) {
		service.delete(currentUser.requireCurrentUser(), damId, gateId);
		return ResponseEntity.noContent().build();
	}
}
