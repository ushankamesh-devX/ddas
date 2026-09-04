package com.damalert.ddas.common.geo;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GeoJsonLineString(
	@NotNull String type,
	@NotNull @Size(min = 2) List<@Size(min = 2, max = 2) List<Double>> coordinates
) {
}
