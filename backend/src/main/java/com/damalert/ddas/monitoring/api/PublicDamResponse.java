package com.damalert.ddas.monitoring.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import com.damalert.ddas.common.geo.GeoJsonPolygon;
import com.damalert.ddas.dam.domain.Dam;
import com.damalert.ddas.dam.domain.DamOperationalState;

public record PublicDamResponse(
	UUID id,
	String code,
	String name,
	String description,
	DamOperationalState operationalState,
	String publicStatusMessage,
	GeoJsonPolygon area
) {
	public static PublicDamResponse from(Dam dam) {
		return new PublicDamResponse(dam.getId(), dam.getCode(), dam.getName(), dam.getDescription(),
			dam.getOperationalState(), dam.getPublicStatusMessage(), polygon(dam.getArea()));
	}

	private static GeoJsonPolygon polygon(Polygon polygon) {
		if (polygon == null) return null;
		List<List<List<Double>>> rings = new ArrayList<>();
		rings.add(ring(polygon.getExteriorRing()));
		for (int i = 0; i < polygon.getNumInteriorRing(); i++) rings.add(ring(polygon.getInteriorRingN(i)));
		return new GeoJsonPolygon("Polygon", rings);
	}

	private static List<List<Double>> ring(LineString ring) {
		return java.util.Arrays.stream(ring.getCoordinates())
			.map(PublicDamResponse::position).toList();
	}

	private static List<Double> position(Coordinate coordinate) {
		return List.of(coordinate.getX(), coordinate.getY());
	}
}
