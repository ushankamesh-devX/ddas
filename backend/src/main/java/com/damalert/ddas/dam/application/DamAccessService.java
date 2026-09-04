package com.damalert.ddas.dam.application;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.error.ForbiddenException;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.domain.DamStaff;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.dam.persistence.DamStaffRepository;

@Service
@Profile("!standalone")
@Transactional(readOnly = true)
public class DamAccessService implements DamAccessChecker {

	private final DamStaffRepository staffRepository;

	public DamAccessService(DamStaffRepository staffRepository) {
		this.staffRepository = staffRepository;
	}

	@Override
	public void requireStaffAccess(CurrentUser user, UUID damId) {
		if (isSuperAdmin(user)) {
			return;
		}
		staffRepository.findByDamIdAndUserId(damId, user.userId())
			.orElseThrow(this::forbidden);
	}

	@Override
	public void requireRole(CurrentUser user, UUID damId, DamStaffRole... roles) {
		if (isSuperAdmin(user)) {
			return;
		}
		Set<DamStaffRole> allowedRoles = Set.copyOf(Arrays.asList(roles));
		DamStaff staff = staffRepository.findByDamIdAndUserId(damId, user.userId())
			.orElseThrow(this::forbidden);
		if (!allowedRoles.contains(staff.getRole())) {
			throw forbidden();
		}
	}

	@Override
	public void requireEmergencyPermission(CurrentUser user, UUID damId) {
		if (isSuperAdmin(user)) {
			return;
		}
		DamStaff staff = staffRepository.findByDamIdAndUserId(damId, user.userId())
			.orElseThrow(this::forbidden);
		if (!staff.canTriggerEmergency()) {
			throw forbidden();
		}
	}

	private boolean isSuperAdmin(CurrentUser user) {
		return user.hasGlobalRole("SUPER_ADMIN");
	}

	private ForbiddenException forbidden() {
		return new ForbiddenException("DAM_ACCESS_DENIED", "You do not have access to this dam.");
	}
}
