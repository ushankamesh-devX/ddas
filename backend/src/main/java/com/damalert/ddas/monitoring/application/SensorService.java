package com.damalert.ddas.monitoring.application;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.audit.AuditEvent;
import com.damalert.ddas.common.audit.AuditService;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.common.geo.GeometryMapper;
import com.damalert.ddas.common.security.CurrentUser;
import com.damalert.ddas.dam.application.DamAccessChecker;
import com.damalert.ddas.dam.application.DamReader;
import com.damalert.ddas.dam.domain.DamStaffRole;
import com.damalert.ddas.monitoring.api.CreateSensorRequest;
import com.damalert.ddas.monitoring.api.LatestReadingResponse;
import com.damalert.ddas.monitoring.api.PublicSensorResponse;
import com.damalert.ddas.monitoring.api.ReadingResponse;
import com.damalert.ddas.monitoring.api.SensorResponse;
import com.damalert.ddas.monitoring.api.UpdateSensorRequest;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.domain.ThresholdDirection;
import com.damalert.ddas.monitoring.persistence.SensorReadingRepository;
import com.damalert.ddas.monitoring.persistence.SensorRepository;

@Service
@Profile("!standalone")
@Transactional
public class SensorService {

	private final SensorRepository sensors;
	private final SensorReadingRepository readings;
	private final DamReader damReader;
	private final DamAccessChecker accessChecker;
	private final GeometryMapper geometryMapper;
	private final AuditService auditService;
	private final Duration staleAfter;

	public SensorService(SensorRepository sensors, SensorReadingRepository readings, DamReader damReader,
		DamAccessChecker accessChecker, GeometryMapper geometryMapper, AuditService auditService,
		@Value("${app.monitoring.stale-after:5m}") Duration staleAfter) {
		this.sensors = sensors;
		this.readings = readings;
		this.damReader = damReader;
		this.accessChecker = accessChecker;
		this.geometryMapper = geometryMapper;
		this.auditService = auditService;
		this.staleAfter = staleAfter;
	}

	@Transactional(readOnly = true)
	public List<SensorResponse> list(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		return sensors.findAllByDamIdOrderByNameAsc(damId).stream().map(SensorResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public SensorResponse get(CurrentUser user, UUID damId, UUID sensorId) {
		accessChecker.requireStaffAccess(user, damId);
		return SensorResponse.from(requireSensor(damId, sensorId));
	}

	public SensorResponse create(CurrentUser user, UUID damId, CreateSensorRequest request) {
		requireManageRole(user, damId);
		damReader.requireDam(damId);
		String code = request.code().trim().toUpperCase();
		if (sensors.existsByDamIdAndCode(damId, code)) {
			throw new ConflictException("SENSOR_CODE_EXISTS", "A sensor with this code already exists for the dam.");
		}
		Point point = request.location() == null ? null : geometryMapper.toPoint(request.location());
		Sensor sensor = new Sensor(UUID.randomUUID(), damId, code, request.name().trim(),
			request.sensorType().trim().toUpperCase(), request.unit().trim(), point,
			request.visibility() == null ? SensorVisibility.PRIVATE : request.visibility(),
			request.exposeExactLocation(), request.warningThreshold(), request.criticalThreshold(),
			request.thresholdDirection() == null ? ThresholdDirection.HIGH : request.thresholdDirection());
		validateThresholds(sensor.getWarningThreshold(), sensor.getCriticalThreshold(), sensor.getThresholdDirection());
		Sensor saved = sensors.saveAndFlush(sensor);
		audit(user, damId, "SENSOR_CREATED", saved.getId(), Map.of("code", saved.getCode()));
		return SensorResponse.from(saved);
	}

	public SensorResponse update(CurrentUser user, UUID damId, UUID sensorId, UpdateSensorRequest request) {
		requireManageRole(user, damId);
		validateThresholds(request.warningThreshold(), request.criticalThreshold(), request.thresholdDirection());
		Sensor sensor = requireSensor(damId, sensorId);
		Point point = request.location() == null ? null : geometryMapper.toPoint(request.location());
		sensor.update(request.name().trim(), request.sensorType().trim().toUpperCase(), request.unit().trim(),
			point, request.visibility(), request.exposeExactLocation(), request.warningThreshold(),
			request.criticalThreshold(), request.thresholdDirection(), request.status());
		audit(user, damId, "SENSOR_UPDATED", sensorId, Map.of("status", request.status().name()));
		return SensorResponse.from(sensor);
	}

	public void delete(CurrentUser user, UUID damId, UUID sensorId) {
		requireManageRole(user, damId);
		Sensor sensor = requireSensor(damId, sensorId);
		sensors.delete(sensor);
		audit(user, damId, "SENSOR_DELETED", sensorId, Map.of("code", sensor.getCode()));
	}

	@Transactional(readOnly = true)
	public List<ReadingResponse> history(CurrentUser user, UUID damId, UUID sensorId, Instant from, Instant to, int size) {
		accessChecker.requireStaffAccess(user, damId);
		requireSensor(damId, sensorId);
		return history(sensorId, from, to, size);
	}

	@Transactional(readOnly = true)
	public List<LatestReadingResponse> latest(CurrentUser user, UUID damId) {
		accessChecker.requireStaffAccess(user, damId);
		return sensors.findAllByDamIdOrderByNameAsc(damId).stream().map(this::latest).toList();
	}

	@Transactional(readOnly = true)
	public List<PublicSensorResponse> listPublic(UUID damId) {
		damReader.requireDam(damId);
		return sensors.findAllByDamIdAndVisibilityNotOrderByNameAsc(damId, SensorVisibility.PRIVATE).stream()
			.map(sensor -> {
				LatestReadingResponse latest = latest(sensor);
				return PublicSensorResponse.from(sensor, latest.status(), latest.measuredAt() == null ? null : latest);
			}).toList();
	}

	@Transactional(readOnly = true)
	public List<ReadingResponse> publicHistory(UUID damId, UUID sensorId, Instant from, Instant to, int size) {
		Sensor sensor = requireSensor(damId, sensorId);
		if (sensor.getVisibility() == SensorVisibility.PRIVATE) {
			throw new NotFoundException("SENSOR_NOT_FOUND", "Sensor does not exist.");
		}
		return history(sensorId, from, to, Math.min(size, 100));
	}

	public Sensor requireSensor(UUID damId, UUID sensorId) {
		return sensors.findByIdAndDamId(sensorId, damId)
			.orElseThrow(() -> new NotFoundException("SENSOR_NOT_FOUND", "Sensor does not exist."));
	}

	public SensorStatus effectiveStatus(Sensor sensor) {
		if (sensor.getStatus() != SensorStatus.ACTIVE) {
			return sensor.getStatus();
		}
		return sensor.getLastSeenAt() == null || sensor.getLastSeenAt().isBefore(Instant.now().minus(staleAfter))
			? SensorStatus.OFFLINE : SensorStatus.ACTIVE;
	}

	private List<ReadingResponse> history(UUID sensorId, Instant from, Instant to, int size) {
		Instant upper = to == null ? Instant.now() : to;
		Instant lower = from == null ? upper.minus(Duration.ofDays(7)) : from;
		if (lower.isAfter(upper)) {
			throw new com.damalert.ddas.common.error.BadRequestException("INVALID_TIME_RANGE", "from must not be after to.");
		}
		int limit = Math.max(1, Math.min(size, 500));
		return readings.findAllBySensorIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(sensorId, lower, upper,
			PageRequest.of(0, limit)).stream().map(ReadingResponse::from).toList();
	}

	private LatestReadingResponse latest(Sensor sensor) {
		return LatestReadingResponse.from(sensor, effectiveStatus(sensor),
			readings.findFirstBySensorIdOrderByMeasuredAtDesc(sensor.getId()).orElse(null));
	}

	private void requireManageRole(CurrentUser user, UUID damId) {
		accessChecker.requireRole(user, damId, DamStaffRole.DAM_ADMIN, DamStaffRole.DAM_ENGINEER);
	}

	private void validateThresholds(java.math.BigDecimal warning, java.math.BigDecimal critical,
		ThresholdDirection direction) {
		if (warning == null || critical == null) return;
		boolean invalid = direction == ThresholdDirection.HIGH
			? warning.compareTo(critical) >= 0 : warning.compareTo(critical) <= 0;
		if (invalid) {
			throw new com.damalert.ddas.common.error.BadRequestException("INVALID_THRESHOLDS",
				"Warning and critical thresholds are inconsistent with the threshold direction.");
		}
	}

	private void audit(CurrentUser user, UUID damId, String action, UUID entityId, Map<String, Object> value) {
		auditService.record(new AuditEvent(damId, user.userId(), action, "sensor", entityId, null, value));
	}
}
