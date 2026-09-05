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
import com.damalert.ddas.evacuation.application.RiskZoneService;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/dams/{damId}/risk-zones")
public class RiskZoneController {

	private final RiskZoneService riskZoneService;
	private final CurrentUserProvider currentUserProvider;
	private final GeometryMapper geometryMapper;

	public RiskZoneController(
		RiskZoneService riskZoneService,
		CurrentUserProvider currentUserProvider,
		GeometryMapper geometryMapper
	) {
		this.riskZoneService = riskZoneService;
		this.currentUserProvider = currentUserProvider;
		this.geometryMapper = geometryMapper;
	}

	@GetMapping
	List<RiskZoneResponse> list(@PathVariable UUID damId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return riskZoneService.list(user, damId).stream()
			.map(zone -> RiskZoneResponse.from(zone, geometryMapper))
			.toList();
	}

	@GetMapping("/{zoneId}")
	RiskZoneResponse get(@PathVariable UUID damId, @PathVariable UUID zoneId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return RiskZoneResponse.from(riskZoneService.get(user, damId, zoneId), geometryMapper);
	}

	@PostMapping
	ResponseEntity<RiskZoneResponse> create(
		@PathVariable UUID damId,
		@Valid @RequestBody CreateRiskZoneRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		RiskZoneResponse created = RiskZoneResponse.from(
			riskZoneService.create(
				user,
				damId,
				request.code(),
				request.name(),
				request.severity(),
				request.geometry(),
				request.evacuationRequiredOrDefault(),
				request.publicVisibleOrDefault(),
				request.instructions()
			),
			geometryMapper
		);
		return ResponseEntity
			.created(URI.create("/api/v1/dams/" + damId + "/risk-zones/" + created.id()))
			.body(created);
	}

	@PatchMapping("/{zoneId}")
	RiskZoneResponse update(
		@PathVariable UUID damId,
		@PathVariable UUID zoneId,
		@Valid @RequestBody UpdateRiskZoneRequest request
	) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		return RiskZoneResponse.from(
			riskZoneService.update(
				user,
				damId,
				zoneId,
				request.name(),
				request.severity(),
				request.geometry(),
				request.evacuationRequired(),
				request.publicVisible(),
				request.instructions(),
				request.active()
			),
			geometryMapper
		);
	}

	@DeleteMapping("/{zoneId}")
	ResponseEntity<Void> delete(@PathVariable UUID damId, @PathVariable UUID zoneId) {
		CurrentUser user = currentUserProvider.requireCurrentUser();
		riskZoneService.delete(user, damId, zoneId);
		return ResponseEntity.noContent().build();
	}
}
