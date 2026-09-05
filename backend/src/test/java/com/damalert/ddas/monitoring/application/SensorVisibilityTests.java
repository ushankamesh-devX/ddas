package com.damalert.ddas.monitoring.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.damalert.ddas.monitoring.api.PublicSensorResponse;
import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorStatus;
import com.damalert.ddas.monitoring.domain.SensorVisibility;
import com.damalert.ddas.monitoring.domain.ThresholdDirection;
import com.damalert.ddas.monitoring.persistence.SensorReadingRepository;
import com.damalert.ddas.monitoring.persistence.SensorRepository;

class SensorVisibilityTests {
	@Test
	void publicSummaryHidesExactLocationAndStartsOfflineUntilFirstReading() {
		Sensor sensor = new Sensor(UUID.randomUUID(), UUID.randomUUID(), "WL-1", "Water level", "WATER_LEVEL",
			"m", new org.locationtech.jts.geom.GeometryFactory().createPoint(new org.locationtech.jts.geom.Coordinate(80, 7)),
			SensorVisibility.PUBLIC_SUMMARY, true, null, null, ThresholdDirection.HIGH);
		SensorService service = new SensorService(mock(SensorRepository.class), mock(SensorReadingRepository.class),
			mock(com.damalert.ddas.dam.application.DamReader.class),
			mock(com.damalert.ddas.dam.application.DamAccessChecker.class),
			mock(com.damalert.ddas.common.geo.GeometryMapper.class),
			mock(com.damalert.ddas.common.audit.AuditService.class), Duration.ofMinutes(5));

		SensorStatus status = service.effectiveStatus(sensor);
		PublicSensorResponse response = PublicSensorResponse.from(sensor, status, null);

		assertThat(status).isEqualTo(SensorStatus.OFFLINE);
		assertThat(response.location()).isNull();
	}
}
