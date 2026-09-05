package com.damalert.ddas.monitoring.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.monitoring.domain.DamGate;
import com.damalert.ddas.monitoring.domain.SensorVisibility;

public interface DamGateRepository extends JpaRepository<DamGate, UUID> {
	List<DamGate> findAllByDamIdOrderByNameAsc(UUID damId);
	List<DamGate> findAllByDamIdAndVisibilityNotOrderByNameAsc(UUID damId, SensorVisibility visibility);
	Optional<DamGate> findByIdAndDamId(UUID id, UUID damId);
	boolean existsByDamIdAndCode(UUID damId, String code);
}
