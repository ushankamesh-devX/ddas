package com.damalert.ddas.evacuation.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

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

import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.common.security.CurrentUserProvider;
import com.damalert.ddas.evacuation.application.EvacuationRouteService;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/evacuation-routes")
public class EvacuationRouteController {

	private final EvacuationRouteService routeService;
	private final CurrentUserProvider currentUserProvider;
	private final GeometryMapper geometryMapper;

	public EvacuationRouteController(
		EvacuationRouteService routeService,
		CurrentUserProvider currentUserProvider,
		GeometryMapper geometryMapper
	) {
		this.routeService = routeService;
		this.currentUserProvider = currentUserProvider;
		this.geometryMapper = geometryMapper;
	}

	@GetMapping
	List<EvacuationRouteResponse> list(@PathVariable UUID damId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return routeService.list(user, damId).stream()
			.map(route -> EvacuationRouteResponse.from(route, geometryMapper))
			.toList();
	}

	@GetMapping("/{routeId}")
	EvacuationRouteResponse get(@PathVariable UUID damId, @PathVariable UUID routeId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return EvacuationRouteResponse.from(routeService.get(user, damId, routeId), geometryMapper);
	}

	@PostMapping
	ResponseEntity<EvacuationRouteResponse> create(
		@PathVariable UUID damId,
		@Valid @RequestBody CreateEvacuationRouteRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		EvacuationRouteResponse created = EvacuationRouteResponse.from(
			routeService.create(
				user,
				damId,
				request.code(),
				request.name(),
				request.fromZoneId(),
				request.safeLocationId(),
				request.geometry(),
				request.routeStatus(),
				request.publicVisibleOrDefault(),
				request.instructions(),
				request.priority()
			),
			geometryMapper
		);
		return ResponseEntity
			.created(URI.create("/api/v1/dams/" + damId + "/evacuation-routes/" + created.id()))
			.body(created);
	}

	@PatchMapping("/{routeId}")
	EvacuationRouteResponse update(
		@PathVariable UUID damId,
		@PathVariable UUID routeId,
		@Valid @RequestBody UpdateEvacuationRouteRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return EvacuationRouteResponse.from(
			routeService.update(
				user,
				damId,
				routeId,
				request.name(),
				request.fromZoneId(),
				request.safeLocationId(),
				request.geometry(),
				request.routeStatus(),
				request.publicVisible(),
				request.instructions(),
				request.priority()
			),
			geometryMapper
		);
	}

	@DeleteMapping("/{routeId}")
	ResponseEntity<Void> delete(@PathVariable UUID damId, @PathVariable UUID routeId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		routeService.delete(user, damId, routeId);
		return ResponseEntity.noContent().build();
	}
}
