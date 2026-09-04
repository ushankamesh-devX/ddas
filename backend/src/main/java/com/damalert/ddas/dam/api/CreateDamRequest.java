package com.damalert.ddas.dam.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDamRequest(
	@NotBlank @Size(max = 64) String code,
	@NotBlank @Size(max = 180) String name,
	String description,
	boolean isPublic
) {
}
