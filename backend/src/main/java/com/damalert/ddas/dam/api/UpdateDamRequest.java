package com.damalert.ddas.dam.api;

import com.damalert.ddas.common.geo.GeoJsonPolygon;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDamRequest(
	@NotBlank @Size(max = 180) String name,
	String description,
	String publicStatusMessage,
	@Valid GeoJsonPolygon area,
	boolean isPublic
) { }
