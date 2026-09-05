package com.damalert.ddas.evacuation.api;

import java.time.Instant;
import java.util.UUID;

import com.damalert.ddas.evacuation.domain.DamEmergencyState;
import com.damalert.ddas.evacuation.domain.EmergencyStatus;

public record EmergencyStateResponse(
	UUID damId,
	EmergencyStatus state,
	UUID activatedBy,
	Instant activatedAt,
	UUID clearedBy,
	Instant clearedAt,
	String reason,
	Instant updatedAt
) {
	public static EmergencyStateResponse from(DamEmergencyState state) {
		return new EmergencyStateResponse(
			state.getDamId(),
			state.getState(),
			state.getActivatedBy(),
			state.getActivatedAt(),
			state.getClearedBy(),
			state.getClearedAt(),
			state.getReason(),
			state.getUpdatedAt()
		);
	}
}
