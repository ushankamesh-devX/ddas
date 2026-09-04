package com.damalert.ddas.dam.api;

import java.util.UUID;

import com.damalert.ddas.dam.application.DamSummary;
import com.damalert.ddas.dam.domain.DamOperationalState;

public record DamResponse(
	UUID id,
	String code,
	String name,
	DamOperationalState operationalState,
	boolean active
) {
	static DamResponse from(DamSummary dam) {
		return new DamResponse(dam.id(), dam.code(), dam.name(), dam.operationalState(), dam.active());
	}
}
