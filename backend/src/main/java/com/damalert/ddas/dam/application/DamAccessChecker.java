package com.damalert.ddas.dam.application;

import java.util.UUID;

import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.domain.DamStaffRole;

public interface DamAccessChecker {

	void requireStaffAccess(CurrentUser user, UUID damId);

	void requireRole(CurrentUser user, UUID damId, DamStaffRole... roles);

	void requireEmergencyPermission(CurrentUser user, UUID damId);
}
