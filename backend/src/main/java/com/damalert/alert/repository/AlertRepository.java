package com.damalert.alert.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.damalert.alert.entity.*;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
	Optional<Alert> findByDamIdAndIdempotencyKey(UUID damId, String idempotencyKey);
	List<Alert> findByDamIdOrderByCreatedAtDesc(UUID damId);
}