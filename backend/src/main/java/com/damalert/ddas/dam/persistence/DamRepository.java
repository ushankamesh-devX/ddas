package com.damalert.ddas.dam.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.ddas.dam.domain.Dam;

public interface DamRepository extends JpaRepository<Dam, UUID> {

	List<Dam> findAllByOrderByNameAsc();

	List<Dam> findAllByIdInOrderByNameAsc(List<UUID> ids);

	List<Dam> findAllByPublicVisibleTrueAndActiveTrueOrderByNameAsc();

	boolean existsByCode(String code);
}
