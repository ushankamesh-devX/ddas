package com.damalert.ddas.dam.application;

import java.util.UUID;

import com.damalert.ddas.dam.domain.DamOperationalState;

public record DamSummary(
	UUID id,
	String code,
	String name,
	DamOperationalState operationalState,
	boolean active
) {
}
