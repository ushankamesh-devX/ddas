package com.damalert.ddas.evacuation.api;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.evacuation.application.PublicEvacuationService;
import com.damalert.ddas.evacuation.application.PublicEvacuationSnapshot;

@RestController
@Profile("!standalone")
@RequestMapping("/api/v1/public/dams/{damId}")
public class PublicEvacuationController {

	private final PublicEvacuationService publicEvacuationService;
	private final GeometryMapper geometryMapper;

	public PublicEvacuationController(
		PublicEvacuationService publicEvacuationService,
		GeometryMapper geometryMapper
	) {
		this.publicEvacuationService = publicEvacuationService;
		this.geometryMapper = geometryMapper;
	}

	@GetMapping("/evacuation")
	PublicEvacuationSnapshotResponse evacuation(@PathVariable UUID damId) {
		PublicEvacuationSnapshot snapshot = publicEvacuationService.snapshot(damId);
		return new PublicEvacuationSnapshotResponse(
			snapshot.damId(),
			snapshot.generatedAt(),
			snapshot.version(),
			snapshot.emergencyActive(),
			snapshot.riskZones().stream()
				.map(zone -> PublicRiskZoneResponse.from(zone, geometryMapper))
				.toList(),
			snapshot.safeLocations().stream()
				.map(safeLocation -> PublicSafeLocationResponse.from(safeLocation, geometryMapper))
				.toList(),
			snapshot.evacuationRoutes().stream()
				.map(route -> PublicEvacuationRouteResponse.from(route, geometryMapper))
				.toList()
		);
	}

	@GetMapping("/safe-locations")
	List<PublicSafeLocationResponse> safeLocations(@PathVariable UUID damId) {
		return publicEvacuationService.publicSafeLocations(damId).stream()
			.map(safeLocation -> PublicSafeLocationResponse.from(safeLocation, geometryMapper))
			.toList();
	}

	@GetMapping("/evacuation-routes")
	List<PublicEvacuationRouteResponse> evacuationRoutes(@PathVariable UUID damId) {
		return publicEvacuationService.publicRoutes(damId).stream()
			.map(route -> PublicEvacuationRouteResponse.from(route, geometryMapper))
			.toList();
	}
}
