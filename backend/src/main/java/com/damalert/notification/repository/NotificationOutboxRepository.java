package com.damalert.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.damalert.notification.entity.NotificationOutbox;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

	@Query(value = """
		SELECT *
		FROM notification_outbox
		WHERE status IN ('PENDING', 'FAILED')
		  AND next_attempt_at <= CURRENT_TIMESTAMP
		ORDER BY next_attempt_at ASC
		LIMIT :batchSize
		FOR UPDATE SKIP LOCKED
		""", nativeQuery = true)
	List<NotificationOutbox> findReadyForProcessing(@Param("batchSize") int batchSize);
}