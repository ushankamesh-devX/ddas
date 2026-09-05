package com.damalert.ddas.evacuation.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.evacuation.domain.EvacuationRoute;
import com.damalert.ddas.evacuation.domain.RiskZone;
import com.damalert.ddas.evacuation.domain.SafeLocation;
import com.damalert.ddas.evacuation.persistence.EvacuationRouteRepository;
import com.damalert.ddas.evacuation.persistence.RiskZoneRepository;
import com.damalert.ddas.evacuation.persistence.SafeLocationRepository;

/**
 * Unauthenticated read model for the public evacuation map.
 *
 * <p>Every query here filters on {@code public_visible} (and {@code is_active} for zones).
 * Public and private operational data is explicit, never inferred.
 */
@Service
@Profile("!standalone")
@Transactional(readOnly = true)
public class PublicEvacuationService {

	private final RiskZoneRepository zoneRepository;
	private final SafeLocationRepository safeLocationRepository;
	private final EvacuationRouteRepository routeRepository;
	private final EmergencyService emergencyService;
	private final DamReader damReader;

	public PublicEvacuationService(
		RiskZoneRepository zoneRepository,
		SafeLocationRepository safeLocationRepository,
		EvacuationRouteRepository routeRepository,
		EmergencyService emergencyService,
		DamReader damReader
	) {
		this.zoneRepository = zoneRepository;
		this.safeLocationRepository = safeLocationRepository;
		this.routeRepository = routeRepository;
		this.emergencyService = emergencyService;
		this.damReader = damReader;
	}

	public PublicEvacuationSnapshot snapshot(UUID damId) {
		damReader.requireDam(damId);

		List<RiskZone> zones = publicZones(damId);
		List<SafeLocation> safeLocations = publicSafeLocations(damId);
		List<EvacuationRoute> routes = publicRoutes(damId);
		boolean emergencyActive = emergencyService.getPublicState(damId).isActive();

		Instant lastChange = Stream.of(
				zones.stream().map(RiskZone::getUpdatedAt),
				safeLocations.stream().map(SafeLocation::getUpdatedAt),
				routes.stream().map(EvacuationRoute::getUpdatedAt)
			)
			.flatMap(stream -> stream)
			.max(Instant::compareTo)
			.orElse(Instant.EPOCH);

		return new PublicEvacuationSnapshot(
			damId,
			Instant.now(),
			Long.toString(lastChange.toEpochMilli()),
			emergencyActive,
			zones,
			safeLocations,
			routes
		);
	}

	public List<RiskZone> publicZones(UUID damId) {
		return zoneRepository.findAllByDamIdAndPublicVisibleTrueAndActiveTrueOrderByCodeAsc(damId);
	}

	public List<SafeLocation> publicSafeLocations(UUID damId) {
		return safeLocationRepository.findAllByDamIdAndPublicVisibleTrueOrderByCodeAsc(damId);
	}

	public List<EvacuationRoute> publicRoutes(UUID damId) {
		return routeRepository.findAllByDamIdAndPublicVisibleTrueOrderByPriorityAscCodeAsc(damId);
	}
}
