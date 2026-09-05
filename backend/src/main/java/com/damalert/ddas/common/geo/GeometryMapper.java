package com.damalert.ddas.common.geo;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import com.damalert.ddas.common.error.BadRequestException;

@Component
public class GeometryMapper {

	public static final int SRID = 4326;

	private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

	public Point toPoint(GeoJsonPoint value) {
		requireType(value.type(), "Point");
		return geometryFactory.createPoint(coordinate(value.coordinates()));
	}

	public LineString toLineString(GeoJsonLineString value) {
		requireType(value.type(), "LineString");
		Coordinate[] coordinates = value.coordinates().stream().map(this::coordinate).toArray(Coordinate[]::new);
		return geometryFactory.createLineString(coordinates);
	}

	public Polygon toPolygon(GeoJsonPolygon value) {
		requireType(value.type(), "Polygon");
		LinearRing shell = ring(value.coordinates().getFirst());
		LinearRing[] holes = value.coordinates().stream().skip(1).map(this::ring).toArray(LinearRing[]::new);
		Polygon polygon = geometryFactory.createPolygon(shell, holes);
		if (!polygon.isValid()) {
			throw invalid("Polygon geometry is invalid or self-intersecting.");
		}
		return polygon;
	}

	public GeoJsonPoint toGeoJson(Point point) {
		return new GeoJsonPoint("Point", position(point.getCoordinate()));
	}

	public GeoJsonLineString toGeoJson(LineString lineString) {
		return new GeoJsonLineString("LineString", positions(lineString.getCoordinates()));
	}

	public GeoJsonPolygon toGeoJson(Polygon polygon) {
		List<List<List<Double>>> rings = new java.util.ArrayList<>();
		rings.add(positions(polygon.getExteriorRing().getCoordinates()));
		for (int index = 0; index < polygon.getNumInteriorRing(); index++) {
			rings.add(positions(polygon.getInteriorRingN(index).getCoordinates()));
		}
		return new GeoJsonPolygon("Polygon", List.copyOf(rings));
	}

	private List<List<Double>> positions(Coordinate[] coordinates) {
		return java.util.Arrays.stream(coordinates).map(this::position).toList();
	}

	private List<Double> position(Coordinate coordinate) {
		return List.of(coordinate.getX(), coordinate.getY());
	}

	private LinearRing ring(List<List<Double>> positions) {
		Coordinate[] coordinates = positions.stream().map(this::coordinate).toArray(Coordinate[]::new);
		if (coordinates.length < 4 || !coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
			throw invalid("Polygon rings must contain at least four positions and must be closed.");
		}
		return geometryFactory.createLinearRing(coordinates);
	}

	private Coordinate coordinate(List<Double> position) {
		if (position == null || position.size() != 2 || position.get(0) == null || position.get(1) == null) {
			throw invalid("A GeoJSON position must contain longitude and latitude.");
		}
		double longitude = position.get(0);
		double latitude = position.get(1);
		if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
			throw invalid("Longitude must be between -180 and 180.");
		}
		if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
			throw invalid("Latitude must be between -90 and 90.");
		}
		return new Coordinate(longitude, latitude);
	}

	private void requireType(String actual, String expected) {
		if (!expected.equals(actual)) {
			throw invalid("Expected GeoJSON type " + expected + ".");
		}
	}

	private BadRequestException invalid(String message) {
		return new BadRequestException("INVALID_GEOMETRY", message);
	}
}
