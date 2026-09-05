package com.damalert.alert.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.alert.entity.Alert;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
	Optional<Alert> findByDamIdAndIdempotencyKey(UUID damId, String idempotencyKey);
}