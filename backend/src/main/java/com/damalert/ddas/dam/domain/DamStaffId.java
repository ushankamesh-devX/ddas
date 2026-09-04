package com.damalert.ddas.dam.domain;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record DamStaffId(
	@Column(name = "dam_id") UUID damId,
	@Column(name = "user_id") UUID userId
) implements Serializable {
}
