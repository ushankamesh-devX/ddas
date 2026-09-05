package com.damalert.notification.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.notification.entity.NotificationOutbox;
import com.damalert.notification.entity.OutboxStatus;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

	List<NotificationOutbox> findTop100ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
		Collection<OutboxStatus> statuses,
		OffsetDateTime now
	);

	List<NotificationOutbox> findByRecipientIdOrderByCreatedAtAsc(UUID recipientId);
}