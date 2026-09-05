package com.damalert.ddas.evacuation.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.evacuation.domain.EvacuationRoute;

public interface EvacuationRouteRepository extends JpaRepository<EvacuationRoute, UUID> {

	List<EvacuationRoute> findAllByDamIdOrderByPriorityAscCodeAsc(UUID damId);

	List<EvacuationRoute> findAllByDamIdAndPublicVisibleTrueOrderByPriorityAscCodeAsc(UUID damId);

	Optional<EvacuationRoute> findByIdAndDamId(UUID id, UUID damId);

	boolean existsByDamIdAndCode(UUID damId, String code);

	boolean existsByFromZoneId(UUID fromZoneId);

	boolean existsBySafeLocationId(UUID safeLocationId);
}
