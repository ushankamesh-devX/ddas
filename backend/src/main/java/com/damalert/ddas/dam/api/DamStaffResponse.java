package com.damalert.ddas.dam.api;

import java.time.Instant;
import java.util.UUID;

import com.damalert.ddas.dam.domain.DamStaff;
import com.damalert.ddas.dam.domain.DamStaffRole;

public record DamStaffResponse(
	UUID userId,
	DamStaffRole role,
	boolean canTriggerEmergency,
	Instant assignedAt
) {
	static DamStaffResponse from(DamStaff staff) {
		return new DamStaffResponse(
			staff.getId().userId(),
			staff.getRole(),
			staff.canTriggerEmergency(),
			staff.getAssignedAt()
		);
	}
}
