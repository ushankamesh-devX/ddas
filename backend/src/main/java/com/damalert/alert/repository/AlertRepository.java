package com.damalert.alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.alert.entity.Alert;
import com.damalert.alert.entity.AlertStatus;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

	List<Alert> findByDamIdOrderByCreatedAtDesc(UUID damId);

	List<Alert> findByDamIdAndStatusOrderByCreatedAtDesc(UUID damId, AlertStatus status);

	Optional<Alert> findByDamIdAndIdempotencyKey(UUID damId, String idempotencyKey);
}