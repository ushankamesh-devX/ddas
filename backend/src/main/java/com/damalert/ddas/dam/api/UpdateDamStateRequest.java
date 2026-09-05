package com.damalert.ddas.dam.api;

import com.damalert.ddas.dam.domain.DamOperationalState;

import jakarta.validation.constraints.NotNull;

public record UpdateDamStateRequest(@NotNull DamOperationalState state, String publicStatusMessage) { }
