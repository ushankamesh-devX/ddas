package com.damalert.alert.repository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.damalert.alert.entity.AlertRecipient;

public interface AlertRecipientRepository extends JpaRepository<AlertRecipient, UUID> {
	Optional<AlertRecipient> findByAlertIdAndUserId(UUID alertId, UUID userId);
}