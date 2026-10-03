package com.damalert.alert.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.alert.dto.CreateAlertDto;
import com.damalert.alert.entity.*;
import com.damalert.alert.repository.*;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.ddas.common.error.NotFoundException;
import com.damalert.notification.entity.*;
import com.damalert.notification.repository.NotificationOutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {
	private final AlertRepository alertRepository;
	private final AlertZoneRepository alertZoneRepository;
	private final AlertRecipientRepository recipientRepository;
	private final NotificationOutboxRepository outboxRepository;

	public Alert createAlert(CreateAlertDto request, UUID creatorId) {
		if (request.idempotencyKey() != null && alertRepository.findByDamIdAndIdempotencyKey(request.damId(), request.idempotencyKey()).isPresent()) {
			throw new ConflictException("ALERT_IDEMPOTENCY_KEY_EXISTS", "An alert with this idempotency key already exists.");
		}
		OffsetDateTime now = OffsetDateTime.now();
		Alert alert = Alert.builder().damId(request.damId()).severity(request.severity()).title(request.title())
			.message(request.message()).recommendedAction(request.recommendedAction()).evacuationRequired(request.evacuationRequired())
			.createdBy(creatorId).expiresAt(request.expiresAt()).idempotencyKey(request.idempotencyKey()).createdAt(now).updatedAt(now).build();
		Alert saved = alertRepository.saveAndFlush(alert);
		request.riskZoneIds().stream().distinct().map(zone -> AlertZone.builder().id(new AlertZoneId(saved.getId(), zone)).build()).forEach(alertZoneRepository::save);
		return saved;
	}

	@Transactional(readOnly = true)
	public List<Alert> listAlerts(UUID damId) { return alertRepository.findByDamIdOrderByCreatedAtDesc(damId); }

	public Alert cancelAlert(UUID alertId, UUID userId) {
		Alert alert = requireAlert(alertId);
		alert.setStatus(AlertStatus.CANCELLED);
		alert.setCancelledBy(userId);
		alert.setCancelledAt(OffsetDateTime.now());
		alert.setUpdatedAt(OffsetDateTime.now());
		return alertRepository.save(alert);
	}

	public void acknowledgeAlert(UUID alertId, UUID userId) {
		AlertRecipient recipient = recipientRepository.findByAlertIdAndUserId(alertId, userId)
			.orElseThrow(() -> new NotFoundException("ALERT_RECIPIENT_NOT_FOUND", "Alert recipient does not exist."));
		recipient.setDeliveryStatus(DeliveryStatus.ACKNOWLEDGED);
		recipient.setAcknowledgedAt(OffsetDateTime.now());
		recipientRepository.save(recipient);
	}

	private Alert requireAlert(UUID alertId) {
		return alertRepository.findById(alertId).orElseThrow(() -> new NotFoundException("ALERT_NOT_FOUND", "Alert does not exist."));
	}
}