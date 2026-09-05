package com.damalert.ddas.monitoring.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.monitoring.domain.Sensor;
import com.damalert.ddas.monitoring.domain.SensorVisibility;

public interface SensorRepository extends JpaRepository<Sensor, UUID> {
	List<Sensor> findAllByDamIdOrderByNameAsc(UUID damId);
	List<Sensor> findAllByDamIdAndVisibilityNotOrderByNameAsc(UUID damId, SensorVisibility visibility);
	Optional<Sensor> findByIdAndDamId(UUID id, UUID damId);
	boolean existsByDamIdAndCode(UUID damId, String code);
}
