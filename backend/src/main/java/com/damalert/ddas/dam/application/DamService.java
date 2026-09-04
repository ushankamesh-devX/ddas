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

@Service
@Profile("!standalone")
@Transactional
public class DamService implements DamReader {

	private final DamRepository damRepository;
	private final DamStaffRepository staffRepository;
	private final DamAccessChecker accessChecker;
	private final AuditService auditService;

	public DamService(
		DamRepository damRepository,
		DamStaffRepository staffRepository,
		DamAccessChecker accessChecker,
		AuditService auditService
	) {
		this.damRepository = damRepository;
		this.staffRepository = staffRepository;
		this.accessChecker = accessChecker;
		this.auditService = auditService;
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

	@Override
	@Transactional(readOnly = true)
	public DamSummary requireDam(UUID damId) {
		return damRepository.findById(damId)
			.map(this::summary)
			.orElseThrow(() -> new NotFoundException("DAM_NOT_FOUND", "Dam does not exist."));
	}

	private DamSummary summary(Dam dam) {
		return new DamSummary(dam.getId(), dam.getCode(), dam.getName(), dam.getOperationalState(), dam.isActive());
	}
}
