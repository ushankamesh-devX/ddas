package com.damalert.alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.damalert.alert.entity.AlertRecipient;
import com.damalert.alert.entity.DeliveryStatus;

public interface AlertRecipientRepository extends JpaRepository<AlertRecipient, UUID> {

	List<AlertRecipient> findByAlertId(UUID alertId);

	List<AlertRecipient> findByAlertIdAndDeliveryStatus(UUID alertId, DeliveryStatus deliveryStatus);

	List<AlertRecipient> findByUserIdOrderByCreatedAtDesc(UUID userId);
}