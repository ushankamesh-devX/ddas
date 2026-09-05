package com.damalert.ddas.monitoring.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.monitoring.domain.SensorReading;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
	Optional<SensorReading> findFirstBySensorIdOrderByMeasuredAtDesc(UUID sensorId);
	List<SensorReading> findAllBySensorIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
		UUID sensorId, Instant from, Instant to, Pageable pageable);
	boolean existsBySensorIdAndExternalMessageId(UUID sensorId, String externalMessageId);
}
