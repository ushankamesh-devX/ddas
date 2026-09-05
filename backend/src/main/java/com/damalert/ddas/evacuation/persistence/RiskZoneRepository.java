package com.damalert.ddas.evacuation.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.evacuation.domain.RiskZone;

public interface RiskZoneRepository extends JpaRepository<RiskZone, UUID> {

	List<RiskZone> findAllByDamIdOrderByCodeAsc(UUID damId);

	List<RiskZone> findAllByDamIdAndPublicVisibleTrueAndActiveTrueOrderByCodeAsc(UUID damId);

	Optional<RiskZone> findByIdAndDamId(UUID id, UUID damId);

	boolean existsByDamIdAndCode(UUID damId, String code);
}
