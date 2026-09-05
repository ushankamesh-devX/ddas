package com.damalert.ddas.monitoring.application;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.ddas.common.error.BadRequestException;
import com.damalert.ddas.common.error.ForbiddenException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.ddas.monitoring.api.LatestReadingResponse;
import com.damalert.ddas.monitoring.domain.DeviceStatus;
import com.damalert.ddas.monitoring.domain.IotDevice;
import com.damalert.ddas.monitoring.domain.ReadingQuality;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorReading;
import com.damalert.ddas.monitoring.domain.SensorStatus;
import com.damalert.ddas.monitoring.persistence.IotDeviceRepository;
import com.damalert.ddas.monitoring.persistence.IotDeviceSensorRepository;
import com.damalert.ddas.monitoring.persistence.SensorReadingRepository;
import com.damalert.ddas.monitoring.persistence.SensorRepository;

@Service
@Profile("!standalone")
@Transactional
public class TelemetryIngestionService {
	private static final Pattern TOPIC = Pattern.compile(
		"^dams/([0-9a-fA-F-]{36})/devices/([0-9a-fA-F-]{36})/telemetry$");

	private final IotDeviceRepository devices;
	private final IotDeviceSensorRepository assignments;
	private final SensorRepository sensors;
	private final SensorReadingRepository readings;
	private final ApplicationEventPublisher events;
	private final Duration maxPastAge;
	private final Duration maxFutureSkew;

	public TelemetryIngestionService(IotDeviceRepository devices, IotDeviceSensorRepository assignments,
		SensorRepository sensors, SensorReadingRepository readings, ApplicationEventPublisher events,
		@Value("${app.monitoring.max-past-age:30d}") Duration maxPastAge,
		@Value("${app.monitoring.max-future-skew:5m}") Duration maxFutureSkew) {
		this.devices = devices;
		this.assignments = assignments;
		this.sensors = sensors;
		this.readings = readings;
		this.events = events;
		this.maxPastAge = maxPastAge;
		this.maxFutureSkew = maxFutureSkew;
	}

	public void ingest(String topic, TelemetryPayload payload, String rawPayload) {
		TopicIdentity identity = parseTopic(topic);
		IotDevice device = devices.findByDeviceUid(identity.deviceId().toString())
			.orElseThrow(() -> new NotFoundException("IOT_DEVICE_NOT_FOUND", "IoT device does not exist."));
		if (!device.getDamId().equals(identity.damId())) {
			throw new ForbiddenException("MQTT_TOPIC_DAM_MISMATCH", "Device is not assigned to the topic dam.");
		}
		if (device.getStatus() != DeviceStatus.ACTIVE) {
			throw new ForbiddenException("IOT_DEVICE_INACTIVE", "IoT device is not active.");
		}
		validatePayload(payload);
		for (TelemetryPayload.TelemetryValue value : payload.readings()) {
			if (!assignments.existsByIdDeviceIdAndIdSensorId(device.getId(), value.sensorId())) {
				throw new ForbiddenException("SENSOR_NOT_ASSIGNED_TO_DEVICE",
					"Telemetry contains a sensor that is not assigned to this device.");
			}
			Sensor sensor = sensors.findByIdAndDamId(value.sensorId(), identity.damId())
				.orElseThrow(() -> new NotFoundException("SENSOR_NOT_FOUND", "Sensor does not exist."));
			if (sensor.getStatus() == SensorStatus.DISABLED) {
				throw new ForbiddenException("SENSOR_DISABLED", "Disabled sensors cannot submit telemetry.");
			}
			if (readings.existsBySensorIdAndExternalMessageId(sensor.getId(), payload.messageId())) {
				continue;
			}
			SensorReading saved = readings.save(new SensorReading(sensor.getId(), payload.measuredAt(), value.value(),
				value.quality() == null ? ReadingQuality.UNKNOWN : value.quality(), payload.messageId(), rawPayload));
			sensor.seenAt(payload.measuredAt());
			events.publishEvent(new TelemetryEvent(identity.damId(),
				LatestReadingResponse.from(sensor, sensor.getStatus(), saved)));
		}
		device.connected();
	}

	private void validatePayload(TelemetryPayload payload) {
		if (payload == null || payload.messageId() == null || payload.messageId().isBlank()
			|| payload.messageId().length() > 160) {
			throw invalid("messageId is required and must be at most 160 characters.");
		}
		if (payload.measuredAt() == null) {
			throw invalid("measuredAt is required.");
		}
		Instant now = Instant.now();
		if (payload.measuredAt().isAfter(now.plus(maxFutureSkew))
			|| payload.measuredAt().isBefore(now.minus(maxPastAge))) {
			throw invalid("measuredAt is outside the accepted time window.");
		}
		if (payload.readings() == null || payload.readings().isEmpty() || payload.readings().size() > 100) {
			throw invalid("readings must contain between 1 and 100 values.");
		}
		for (TelemetryPayload.TelemetryValue reading : payload.readings()) {
			if (reading == null || reading.sensorId() == null || reading.value() == null) {
				throw invalid("Each reading requires sensorId and value.");
			}
			BigDecimal value = reading.value();
			if (value.precision() > 20 || Math.max(0, value.scale()) > 8) {
				throw invalid("Reading values support at most 20 digits and 8 decimal places.");
			}
		}
	}

	private TopicIdentity parseTopic(String topic) {
		Matcher matcher = TOPIC.matcher(topic == null ? "" : topic);
		if (!matcher.matches()) {
			throw invalid("MQTT topic must match dams/{damId}/devices/{deviceId}/telemetry.");
		}
		try {
			return new TopicIdentity(UUID.fromString(matcher.group(1)), UUID.fromString(matcher.group(2)));
		}
		catch (IllegalArgumentException ex) {
			throw invalid("MQTT topic contains an invalid identifier.");
		}
	}

	private BadRequestException invalid(String message) {
		return new BadRequestException("INVALID_TELEMETRY", message);
	}

	private record TopicIdentity(UUID damId, UUID deviceId) { }
}
