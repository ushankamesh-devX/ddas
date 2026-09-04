package com.damalert.ddas.common.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.damalert.ddas.common.error.BadRequestException;

class GeometryMapperTests {

	private final GeometryMapper mapper = new GeometryMapper();

	@Test
	void mapsLongitudeLatitudePointWithSrid4326() {
		var point = mapper.toPoint(new GeoJsonPoint("Point", List.of(80.1234, 7.1234)));

		assertThat(point.getX()).isEqualTo(80.1234);
		assertThat(point.getY()).isEqualTo(7.1234);
		assertThat(point.getSRID()).isEqualTo(GeometryMapper.SRID);
	}

	@Test
	void rejectsLatitudeOutsideWorldBounds() {
		assertThatThrownBy(() -> mapper.toPoint(new GeoJsonPoint("Point", List.of(80.0, 91.0))))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("Latitude must be between -90 and 90.");
	}

	@Test
	void rejectsUnclosedPolygonRing() {
		GeoJsonPolygon polygon = new GeoJsonPolygon("Polygon", List.of(List.of(
			List.of(80.0, 7.0),
			List.of(80.1, 7.0),
			List.of(80.1, 7.1),
			List.of(80.0, 7.1)
		)));

		assertThatThrownBy(() -> mapper.toPolygon(polygon))
			.isInstanceOf(BadRequestException.class)
			.hasMessageContaining("must be closed");
	}
}
