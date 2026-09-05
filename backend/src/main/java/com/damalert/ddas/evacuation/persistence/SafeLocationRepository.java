package com.damalert.ddas.evacuation.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.evacuation.domain.SafeLocation;

public interface SafeLocationRepository extends JpaRepository<SafeLocation, UUID> {

	List<SafeLocation> findAllByDamIdOrderByCodeAsc(UUID damId);

	List<SafeLocation> findAllByDamIdAndPublicVisibleTrueOrderByCodeAsc(UUID damId);

	Optional<SafeLocation> findByIdAndDamId(UUID id, UUID damId);

	boolean existsByDamIdAndCode(UUID damId, String code);
}
