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
import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.evacuation.domain.RiskZone;
import com.damalert.ddas.evacuation.domain.RiskZoneSeverity;
import com.damalert.ddas.evacuation.persistence.EvacuationRouteRepository;
import com.damalert.ddas.evacuation.persistence.RiskZoneRepository;

@Service
@Profile("!standalone")
@Transactional
public class RiskZoneService {

	private static final DamStaffRole[] WRITE_ROLES = {DamStaffRole.DAM_ADMIN, DamStaffRole.DAM_ENGINEER};

	private final RiskZoneRepository zoneRepository;
	private final EvacuationRouteRepository routeRepository;
	private final DamAccessChecker accessChecker;
	private final DamReader damReader;
	private final GeometryMapper geometryMapper;
	private final AuditService auditService;

	public RiskZoneService(
		RiskZoneRepository zoneRepository,
		EvacuationRouteRepository routeRepository,
		DamAccessChecker accessChecker,
		DamReader damReader,
		GeometryMapper geometryMapper,
		AuditService auditService
	) {
		this.zoneRepository = zoneRepository;
		this.routeRepository = routeRepository;
		this.accessChecker = accessChecker;
		this.damReader = damReader;
		this.geometryMapper = geometryMapper;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<RiskZone> list(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		return zoneRepository.findAllByDamIdOrderByCodeAsc(damId);
	}

	@Transactional(readOnly = true)
	public RiskZone get(CurrentUser user, UUID damId, UUID zoneId) {
		accessChecker.requireStaffAccess(user, damId);
		return require(damId, zoneId);
	}

	public RiskZone create(
		CurrentUser user,
		UUID damId,
		String code,
		String name,
		RiskZoneSeverity severity,
		GeoJsonPolygon geometry,
		boolean evacuationRequired,
		boolean publicVisible,
		String instructions
	) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		damReader.requireDam(damId);

		String normalizedCode = code.trim().toUpperCase();
		if (zoneRepository.existsByDamIdAndCode(damId, normalizedCode)) {
			throw new ConflictException("RISK_ZONE_CODE_EXISTS", "A risk zone with this code already exists for the dam.");
		}

		RiskZone zone = new RiskZone(
			damId,
			normalizedCode,
			name.trim(),
			severity,
			geometryMapper.toPolygon(geometry),
			evacuationRequired,
			publicVisible,
			instructions
		);
		RiskZone saved = zoneRepository.saveAndFlush(zone);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"RISK_ZONE_CREATED",
			"risk_zone",
			saved.getId(),
			null,
			Map.of("code", saved.getCode(), "severity", saved.getSeverity().name())
		));
		return saved;
	}

	public RiskZone update(
		CurrentUser user,
		UUID damId,
		UUID zoneId,
		String name,
		RiskZoneSeverity severity,
		GeoJsonPolygon geometry,
		Boolean evacuationRequired,
		Boolean publicVisible,
		String instructions,
		Boolean active
	) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		RiskZone zone = require(damId, zoneId);
		Map<String, Object> before = Map.of(
			"severity", zone.getSeverity().name(),
			"active", zone.isActive(),
			"publicVisible", zone.isPublicVisible()
		);

		zone.update(
			name == null ? null : name.trim(),
			severity,
			geometry == null ? null : geometryMapper.toPolygon(geometry),
			evacuationRequired,
			publicVisible,
			instructions,
			active
		);
		RiskZone saved = zoneRepository.saveAndFlush(zone);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"RISK_ZONE_UPDATED",
			"risk_zone",
			saved.getId(),
			before,
			Map.of(
				"severity", saved.getSeverity().name(),
				"active", saved.isActive(),
				"publicVisible", saved.isPublicVisible()
			)
		));
		return saved;
	}

	public void delete(CurrentUser user, UUID damId, UUID zoneId) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		RiskZone zone = require(damId, zoneId);
		if (routeRepository.existsByFromZoneId(zoneId)) {
			throw new ConflictException(
				"RISK_ZONE_IN_USE",
				"An evacuation route still references this risk zone."
			);
		}
		zoneRepository.delete(zone);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"RISK_ZONE_DELETED",
			"risk_zone",
			zoneId,
			Map.of("code", zone.getCode()),
			null
		));
	}

	/**
	 * Authoritative zone lookup for other modules. Alert targeting consumes risk-zone
	 * identities rather than creating shadow copies.
	 */
	@Transactional(readOnly = true)
	public RiskZone require(UUID damId, UUID zoneId) {
		return zoneRepository.findByIdAndDamId(zoneId, damId)
			.orElseThrow(() -> new NotFoundException("RISK_ZONE_NOT_FOUND", "Risk zone does not exist for this dam."));
	}
}
