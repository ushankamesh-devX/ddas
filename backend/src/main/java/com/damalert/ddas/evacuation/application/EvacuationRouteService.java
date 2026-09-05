package com.damalert.ddas.evacuation.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.common.geo.GeoJsonLineString;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.evacuation.domain.EvacuationRoute;
import com.damalert.ddas.evacuation.domain.RouteStatus;
import com.damalert.ddas.evacuation.persistence.EvacuationRouteRepository;

@Service
@Profile("!standalone")
@Transactional
public class EvacuationRouteService {

	private static final DamStaffRole[] WRITE_ROLES = {DamStaffRole.DAM_ADMIN, DamStaffRole.DAM_ENGINEER};

	private final EvacuationRouteRepository routeRepository;
	private final RiskZoneService riskZoneService;
	private final SafeLocationService safeLocationService;
	private final DamAccessChecker accessChecker;
	private final DamReader damReader;
	private final GeometryMapper geometryMapper;
	private final AuditService auditService;

	public EvacuationRouteService(
		EvacuationRouteRepository routeRepository,
		RiskZoneService riskZoneService,
		SafeLocationService safeLocationService,
		DamAccessChecker accessChecker,
		DamReader damReader,
		GeometryMapper geometryMapper,
		AuditService auditService
	) {
		this.routeRepository = routeRepository;
		this.riskZoneService = riskZoneService;
		this.safeLocationService = safeLocationService;
		this.accessChecker = accessChecker;
		this.damReader = damReader;
		this.geometryMapper = geometryMapper;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<EvacuationRoute> list(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		return routeRepository.findAllByDamIdOrderByPriorityAscCodeAsc(damId);
	}

	@Transactional(readOnly = true)
	public EvacuationRoute get(CurrentUser user, UUID damId, UUID routeId) {
		accessChecker.requireStaffAccess(user, damId);
		return require(damId, routeId);
	}

	public EvacuationRoute create(
		CurrentUser user,
		UUID damId,
		String code,
		String name,
		UUID fromZoneId,
		UUID safeLocationId,
		GeoJsonLineString geometry,
		RouteStatus routeStatus,
		boolean publicVisible,
		String instructions,
		Short priority
	) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		damReader.requireDam(damId);

		// Both endpoints must belong to this dam. A zone or safe location from another
		// dam resolves to nothing here, which is what blocks cross-dam route creation.
		riskZoneService.require(damId, fromZoneId);
		safeLocationService.require(damId, safeLocationId);

		String normalizedCode = code.trim().toUpperCase();
		if (routeRepository.existsByDamIdAndCode(damId, normalizedCode)) {
			throw new ConflictException(
				"EVACUATION_ROUTE_CODE_EXISTS",
				"An evacuation route with this code already exists for the dam."
			);
		}

		EvacuationRoute route = new EvacuationRoute(
			damId,
			normalizedCode,
			name.trim(),
			fromZoneId,
			safeLocationId,
			geometryMapper.toLineString(geometry),
			routeStatus == null ? RouteStatus.ACTIVE : routeStatus,
			publicVisible,
			instructions,
			priority == null ? (short) 1 : priority
		);
		EvacuationRoute saved = routeRepository.saveAndFlush(route);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"EVACUATION_ROUTE_CREATED",
			"evacuation_route",
			saved.getId(),
			null,
			Map.of(
				"code", saved.getCode(),
				"fromZoneId", saved.getFromZoneId().toString(),
				"safeLocationId", saved.getSafeLocationId().toString()
			)
		));
		return saved;
	}

	public EvacuationRoute update(
		CurrentUser user,
		UUID damId,
		UUID routeId,
		String name,
		UUID fromZoneId,
		UUID safeLocationId,
		GeoJsonLineString geometry,
		RouteStatus routeStatus,
		Boolean publicVisible,
		String instructions,
		Short priority
	) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		EvacuationRoute route = require(damId, routeId);
		if (fromZoneId != null) {
			riskZoneService.require(damId, fromZoneId);
		}
		if (safeLocationId != null) {
			safeLocationService.require(damId, safeLocationId);
		}
		Map<String, Object> before = Map.of("routeStatus", route.getRouteStatus().name());

		route.update(
			name == null ? null : name.trim(),
			fromZoneId,
			safeLocationId,
			geometry == null ? null : geometryMapper.toLineString(geometry),
			routeStatus,
			publicVisible,
			instructions,
			priority
		);
		EvacuationRoute saved = routeRepository.saveAndFlush(route);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"EVACUATION_ROUTE_UPDATED",
			"evacuation_route",
			saved.getId(),
			before,
			Map.of("routeStatus", saved.getRouteStatus().name())
		));
		return saved;
	}

	public void delete(CurrentUser user, UUID damId, UUID routeId) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		EvacuationRoute route = require(damId, routeId);
		routeRepository.delete(route);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"EVACUATION_ROUTE_DELETED",
			"evacuation_route",
			routeId,
			Map.of("code", route.getCode()),
			null
		));
	}

	@Transactional(readOnly = true)
	public EvacuationRoute require(UUID damId, UUID routeId) {
		return routeRepository.findByIdAndDamId(routeId, damId)
			.orElseThrow(() -> new NotFoundException(
				"EVACUATION_ROUTE_NOT_FOUND",
				"Evacuation route does not exist for this dam."
			));
	}
}
