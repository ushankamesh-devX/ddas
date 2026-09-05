package com.damalert.ddas.evacuation.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.common.geo.GeoJsonLineString;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.evacuation.domain.EvacuationRoute;
import com.damalert.ddas.evacuation.persistence.EvacuationRouteRepository;

class EvacuationRouteServiceTests {

	private final EvacuationRouteRepository routeRepository = mock(EvacuationRouteRepository.class);
	private final RiskZoneService riskZoneService = mock(RiskZoneService.class);
	private final SafeLocationService safeLocationService = mock(SafeLocationService.class);
	private final DamAccessChecker accessChecker = mock(DamAccessChecker.class);
	private final DamReader damReader = mock(DamReader.class);
	private final AuditService auditService = mock(AuditService.class);
	private final EvacuationRouteService service = new EvacuationRouteService(
		routeRepository,
		riskZoneService,
		safeLocationService,
		accessChecker,
		damReader,
		new GeometryMapper(),
		auditService
	);

	private final UUID damId = UUID.randomUUID();
	private final CurrentUser admin = new CurrentUser(UUID.randomUUID(), "admin@example.test", Set.of("SUPER_ADMIN"));
	private final GeoJsonLineString geometry = new GeoJsonLineString(
		"LineString",
		List.of(List.of(80.1, 7.1), List.of(80.2, 7.2))
	);

	@Test
	void aRouteCannotStartFromAnotherDamsRiskZone() {
		UUID otherDamZoneId = UUID.randomUUID();
		when(riskZoneService.require(damId, otherDamZoneId))
			.thenThrow(new NotFoundException("RISK_ZONE_NOT_FOUND", "Risk zone does not exist for this dam."));

		assertThatThrownBy(() -> service.create(
			admin, damId, "BAD-ROUTE", "Cross-dam route",
			otherDamZoneId, UUID.randomUUID(), geometry, null, true, null, null
		)).isInstanceOf(NotFoundException.class);

		verify(routeRepository, never()).saveAndFlush(any(EvacuationRoute.class));
	}

	@Test
	void aRouteCannotEndAtAnotherDamsSafeLocation() {
		UUID otherDamSafeLocationId = UUID.randomUUID();
		when(safeLocationService.require(damId, otherDamSafeLocationId))
			.thenThrow(new NotFoundException("SAFE_LOCATION_NOT_FOUND", "Safe location does not exist for this dam."));

		assertThatThrownBy(() -> service.create(
			admin, damId, "BAD-ROUTE", "Cross-dam route",
			UUID.randomUUID(), otherDamSafeLocationId, geometry, null, true, null, null
		)).isInstanceOf(NotFoundException.class);

		verify(routeRepository, never()).saveAndFlush(any(EvacuationRoute.class));
	}
}
