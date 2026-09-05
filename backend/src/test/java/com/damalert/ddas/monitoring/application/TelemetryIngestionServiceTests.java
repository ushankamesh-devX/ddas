package com.damalert.ddas.monitoring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.damalert.ddas.common.error.ForbiddenException;
import com.damalert.ddas.monitoring.domain.DeviceAuthMethod;
import com.damalert.ddas.monitoring.domain.DeviceType;
import com.damalert.ddas.monitoring.domain.IotDevice;
import com.damalert.ddas.monitoring.domain.ReadingQuality;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorReading;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.domain.ThresholdDirection;
import com.damalert.ddas.monitoring.persistence.IotDeviceRepository;
import com.damalert.ddas.monitoring.persistence.IotDeviceSensorRepository;
import com.damalert.ddas.monitoring.persistence.SensorReadingRepository;
import com.damalert.ddas.monitoring.persistence.SensorRepository;

class TelemetryIngestionServiceTests {
	private final UUID damId = UUID.randomUUID();
	private final UUID deviceId = UUID.randomUUID();
	private final UUID sensorId = UUID.randomUUID();
	private IotDeviceRepository devices;
	private IotDeviceSensorRepository assignments;
	private SensorRepository sensors;
	private SensorReadingRepository readings;
	private ApplicationEventPublisher events;
	private TelemetryIngestionService service;

	@BeforeEach
	void setUp() {
		devices = mock(IotDeviceRepository.class);
		assignments = mock(IotDeviceSensorRepository.class);
		sensors = mock(SensorRepository.class);
		readings = mock(SensorReadingRepository.class);
		events = mock(ApplicationEventPublisher.class);
		service = new TelemetryIngestionService(devices, assignments, sensors, readings, events,
			Duration.ofDays(30), Duration.ofMinutes(5));
		IotDevice device = new IotDevice(deviceId, damId, "Gateway", DeviceType.GATEWAY, DeviceAuthMethod.DEVICE_KEY);
		Sensor sensor = new Sensor(sensorId, damId, "WL-1", "Water level", "WATER_LEVEL", "m", null,
			SensorVisibility.PUBLIC_SUMMARY, false, null, null, ThresholdDirection.HIGH);
		when(devices.findByDeviceUid(deviceId.toString())).thenReturn(Optional.of(device));
		when(sensors.findByIdAndDamId(sensorId, damId)).thenReturn(Optional.of(sensor));
		when(readings.save(any(SensorReading.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void acceptsAssignedSensorAndPublishesEvent() {
		when(assignments.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);
		when(readings.existsBySensorIdAndExternalMessageId(sensorId, "message-1")).thenReturn(false);

		service.ingest(topic(), payload("message-1"), "{}");

		verify(readings).save(any(SensorReading.class));
		verify(events).publishEvent(any(TelemetryEvent.class));
		assertThat(devices.findByDeviceUid(deviceId.toString()).orElseThrow().getLastConnectedAt()).isNotNull();
	}

	@Test
	void rejectsSensorThatIsNotAssignedToDevice() {
		when(assignments.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(false);

		assertThatThrownBy(() -> service.ingest(topic(), payload("message-2"), "{}"))
			.isInstanceOf(ForbiddenException.class)
			.hasMessageContaining("not assigned");
		verify(readings, never()).save(any());
	}

	@Test
	void duplicateMessageIsIdempotent() {
		when(assignments.existsByIdDeviceIdAndIdSensorId(deviceId, sensorId)).thenReturn(true);
		when(readings.existsBySensorIdAndExternalMessageId(sensorId, "message-3")).thenReturn(true);

		service.ingest(topic(), payload("message-3"), "{}");

		verify(readings, never()).save(any());
		verify(events, never()).publishEvent(any());
	}

	private String topic() {
		return "dams/" + damId + "/devices/" + deviceId + "/telemetry";
	}

	private TelemetryPayload payload(String messageId) {
		return new TelemetryPayload(messageId, Instant.now(),
			List.of(new TelemetryPayload.TelemetryValue(sensorId, new BigDecimal("81.4"), ReadingQuality.GOOD)));
	}
}
