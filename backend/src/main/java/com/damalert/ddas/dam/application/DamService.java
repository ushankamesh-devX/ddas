package com.damalert.ddas.dam.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.ddas.common.error.ForbiddenException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.domain.Dam;
import com.damalert.ddas.dam.persistence.DamRepository;
import com.damalert.ddas.dam.persistence.DamStaffRepository;
import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.dam.domain.DamOperationalState;
import com.damalert.ddas.dam.domain.DamStaffRole;

@Service
@Profile("!standalone")
@Transactional
public class DamService implements DamReader {

	private final DamRepository damRepository;
	private final DamStaffRepository staffRepository;
	private final DamAccessChecker accessChecker;
	private final AuditService auditService;
	private final GeometryMapper geometryMapper;

	public DamService(
		DamRepository damRepository,
		DamStaffRepository staffRepository,
		DamAccessChecker accessChecker,
		AuditService auditService,
		GeometryMapper geometryMapper
	) {
		this.damRepository = damRepository;
		this.staffRepository = staffRepository;
		this.accessChecker = accessChecker;
		this.auditService = auditService;
		this.geometryMapper = geometryMapper;
	}

	@Transactional(readOnly = true)
	public List<DamSummary> listAccessible(CurrentUser user) {
		List<Dam> dams;
		if (user.hasGlobalRole("SUPER_ADMIN")) {
			dams = damRepository.findAllByOrderByNameAsc();
		}
		else {
			List<UUID> damIds = staffRepository.findAllByUserId(user.userId()).stream()
				.map(staff -> staff.getId().damId())
				.toList();
			if (damIds.isEmpty()) {
				throw new ForbiddenException("DAM_ACCESS_DENIED", "You do not have staff access to a dam.");
			}
			dams = damRepository.findAllByIdInOrderByNameAsc(damIds);
		}
		return dams.stream().map(this::summary).toList();
	}

	@Transactional(readOnly = true)
	public DamSummary getAccessible(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		return requireDam(damId);
	}

	public DamSummary create(CurrentUser user, String code, String name, String description, boolean publicVisible) {
		if (!user.hasGlobalRole("SUPER_ADMIN")) {
			throw new ForbiddenException("DAM_CREATE_FORBIDDEN", "Only a platform administrator can create a dam.");
		}
		String normalizedCode = code.trim().toUpperCase();
		if (damRepository.existsByCode(normalizedCode)) {
			throw new ConflictException("DAM_CODE_EXISTS", "A dam with this code already exists.");
		}
		Dam dam = new Dam(UUID.randomUUID(), normalizedCode, name.trim(), description, publicVisible);
		Dam saved = damRepository.saveAndFlush(dam);
		auditService.record(new AuditEvent(
			saved.getId(),
			user.userId(),
			"DAM_CREATED",
			"dam",
			saved.getId(),
			null,
			Map.of("code", saved.getCode(), "name", saved.getName())
		));
		return summary(saved);
	}

	public DamSummary update(CurrentUser user, UUID damId, String name, String description,
		String publicStatusMessage, GeoJsonPolygon area, boolean publicVisible) {
		accessChecker.requireRole(user, damId, DamStaffRole.DAM_ADMIN);
		Dam dam = requireEntity(damId);
		dam.update(name.trim(), description, publicStatusMessage,
			area == null ? null : geometryMapper.toPolygon(area), publicVisible);
		auditService.record(new AuditEvent(damId, user.userId(), "DAM_UPDATED", "dam", damId, null,
			Map.of("name", dam.getName(), "isPublic", dam.isPublicVisible())));
		return summary(dam);
	}

	public DamSummary updateState(CurrentUser user, UUID damId, DamOperationalState state, String publicStatusMessage) {
		accessChecker.requireRole(user, damId, DamStaffRole.DAM_ADMIN, DamStaffRole.DAM_OPERATOR);
		Dam dam = requireEntity(damId);
		dam.setOperationalState(state, publicStatusMessage);
		auditService.record(new AuditEvent(damId, user.userId(), "DAM_STATE_UPDATED", "dam", damId, null,
			Map.of("state", state.name())));
		return summary(dam);
	}

	public void deactivate(CurrentUser user, UUID damId) {
		if (!user.hasGlobalRole("SUPER_ADMIN")) {
			throw new ForbiddenException("DAM_DELETE_FORBIDDEN", "Only a platform administrator can deactivate a dam.");
		}
		Dam dam = requireEntity(damId);
		dam.deactivate();
		auditService.record(new AuditEvent(damId, user.userId(), "DAM_DEACTIVATED", "dam", damId, null, Map.of()));
	}

	@Override
	@Transactional(readOnly = true)
	public DamSummary requireDam(UUID damId) {
		return summary(requireEntity(damId));
	}

	private Dam requireEntity(UUID damId) {
		return damRepository.findById(damId)
			.orElseThrow(() -> new NotFoundException("DAM_NOT_FOUND", "Dam does not exist."));
	}

	private DamSummary summary(Dam dam) {
		return new DamSummary(dam.getId(), dam.getCode(), dam.getName(), dam.getOperationalState(), dam.isActive());
	}
}
