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
import com.damalert.ddas.common.geo.GeoJsonPoint;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.evacuation.domain.SafeLocation;
import com.damalert.ddas.evacuation.domain.SafeLocationStatus;
import com.damalert.ddas.evacuation.persistence.EvacuationRouteRepository;
import com.damalert.ddas.evacuation.persistence.SafeLocationRepository;

@Service
@Profile("!standalone")
@Transactional
public class SafeLocationService {

	private static final DamStaffRole[] WRITE_ROLES = {
		DamStaffRole.DAM_ADMIN,
		DamStaffRole.DAM_ENGINEER,
		DamStaffRole.FIELD_OFFICER
	};

	private final SafeLocationRepository safeLocationRepository;
	private final EvacuationRouteRepository routeRepository;
	private final DamAccessChecker accessChecker;
	private final DamReader damReader;
	private final GeometryMapper geometryMapper;
	private final AuditService auditService;

	public SafeLocationService(
		SafeLocationRepository safeLocationRepository,
		EvacuationRouteRepository routeRepository,
		DamAccessChecker accessChecker,
		DamReader damReader,
		GeometryMapper geometryMapper,
		AuditService auditService
	) {
		this.safeLocationRepository = safeLocationRepository;
		this.routeRepository = routeRepository;
		this.accessChecker = accessChecker;
		this.damReader = damReader;
		this.geometryMapper = geometryMapper;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<SafeLocation> list(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		return safeLocationRepository.findAllByDamIdOrderByCodeAsc(damId);
	}

	@Transactional(readOnly = true)
	public SafeLocation get(CurrentUser user, UUID damId, UUID safeLocationId) {
		accessChecker.requireStaffAccess(user, damId);
		return require(damId, safeLocationId);
	}

	public SafeLocation create(
		CurrentUser user,
		UUID damId,
		String code,
		String name,
		GeoJsonPoint location,
		Integer capacity,
		Integer currentOccupancy,
		SafeLocationStatus status,
		String contactNumber,
		List<String> facilities,
		boolean publicVisible,
		String instructions
	) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		damReader.requireDam(damId);
		requireOccupancyWithinCapacity(capacity, currentOccupancy);

		String normalizedCode = code.trim().toUpperCase();
		if (safeLocationRepository.existsByDamIdAndCode(damId, normalizedCode)) {
			throw new ConflictException(
				"SAFE_LOCATION_CODE_EXISTS",
				"A safe location with this code already exists for the dam."
			);
		}

		SafeLocation safeLocation = new SafeLocation(
			damId,
			normalizedCode,
			name.trim(),
			geometryMapper.toPoint(location),
			capacity,
			currentOccupancy,
			status == null ? SafeLocationStatus.AVAILABLE : status,
			contactNumber,
			facilities,
			publicVisible,
			instructions
		);
		SafeLocation saved = safeLocationRepository.saveAndFlush(safeLocation);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"SAFE_LOCATION_CREATED",
			"safe_location",
			saved.getId(),
			null,
			Map.of("code", saved.getCode(), "status", saved.getStatus().name())
		));
		return saved;
	}

	public SafeLocation update(
		CurrentUser user,
		UUID damId,
		UUID safeLocationId,
		String name,
		GeoJsonPoint location,
		Integer capacity,
		Integer currentOccupancy,
		SafeLocationStatus status,
		String contactNumber,
		List<String> facilities,
		Boolean publicVisible,
		String instructions
	) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		SafeLocation safeLocation = require(damId, safeLocationId);
		requireOccupancyWithinCapacity(
			capacity == null ? safeLocation.getCapacity() : capacity,
			currentOccupancy == null ? safeLocation.getCurrentOccupancy() : currentOccupancy
		);
		Map<String, Object> before = Map.of("status", safeLocation.getStatus().name());

		safeLocation.update(
			name == null ? null : name.trim(),
			location == null ? null : geometryMapper.toPoint(location),
			capacity,
			currentOccupancy,
			status,
			contactNumber,
			facilities,
			publicVisible,
			instructions
		);
		SafeLocation saved = safeLocationRepository.saveAndFlush(safeLocation);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"SAFE_LOCATION_UPDATED",
			"safe_location",
			saved.getId(),
			before,
			Map.of("status", saved.getStatus().name())
		));
		return saved;
	}

	public void delete(CurrentUser user, UUID damId, UUID safeLocationId) {
		accessChecker.requireRole(user, damId, WRITE_ROLES);
		SafeLocation safeLocation = require(damId, safeLocationId);
		if (routeRepository.existsBySafeLocationId(safeLocationId)) {
			throw new ConflictException(
				"SAFE_LOCATION_IN_USE",
				"An evacuation route still references this safe location."
			);
		}
		safeLocationRepository.delete(safeLocation);
		auditService.record(new AuditEvent(
			damId,
			user.userId(),
			"SAFE_LOCATION_DELETED",
			"safe_location",
			safeLocationId,
			Map.of("code", safeLocation.getCode()),
			null
		));
	}

	@Transactional(readOnly = true)
	public SafeLocation require(UUID damId, UUID safeLocationId) {
		return safeLocationRepository.findByIdAndDamId(safeLocationId, damId)
			.orElseThrow(() -> new NotFoundException(
				"SAFE_LOCATION_NOT_FOUND",
				"Safe location does not exist for this dam."
			));
	}

	private void requireOccupancyWithinCapacity(Integer capacity, Integer currentOccupancy) {
		if (capacity != null && currentOccupancy != null && currentOccupancy > capacity) {
			throw new com.damalert.ddas.common.error.BadRequestException(
				"OCCUPANCY_EXCEEDS_CAPACITY",
				"Current occupancy cannot be greater than capacity."
			);
		}
	}
}
