package com.damalert.alert.service;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.alert.dto.CreateAlertDto;
import com.damalert.alert.entity.Alert;
import com.damalert.alert.entity.AlertRecipient;
import com.damalert.alert.entity.AlertZone;
import com.damalert.alert.entity.AlertZoneId;
import com.damalert.alert.entity.DeliveryStatus;
import com.damalert.alert.repository.AlertRecipientRepository;
import com.damalert.alert.repository.AlertRepository;
import com.damalert.alert.repository.AlertZoneRepository;
import com.damalert.ddas.common.error.ConflictException;
import com.damalert.notification.entity.Channel;
import com.damalert.notification.entity.NotificationOutbox;
import com.damalert.notification.entity.OutboxStatus;
import com.damalert.notification.repository.NotificationOutboxRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

	private final AlertRepository alertRepository;
	private final AlertZoneRepository alertZoneRepository;
	private final AlertRecipientRepository alertRecipientRepository;
	private final NotificationOutboxRepository notificationOutboxRepository;
	private final ObjectMapper objectMapper;

	public Alert createAlert(CreateAlertDto request, UUID creatorId) {
		if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()
			&& alertRepository.findByDamIdAndIdempotencyKey(request.damId(), request.idempotencyKey()).isPresent()) {
			throw new ConflictException("ALERT_IDEMPOTENCY_KEY_EXISTS", "An alert with this idempotency key already exists.");
		}

		OffsetDateTime now = OffsetDateTime.now();
		Alert alert = Alert.builder()
			.damId(request.damId())
			.severity(request.severity())
			.title(request.title())
			.message(request.message())
			.recommendedAction(request.recommendedAction())
			.evacuationRequired(request.evacuationRequired())
			.createdBy(creatorId)
			.expiresAt(request.expiresAt())
			.idempotencyKey(request.idempotencyKey())
			.createdAt(now)
			.updatedAt(now)
			.build();
		Alert savedAlert = alertRepository.saveAndFlush(alert);

		List<AlertZone> alertZones = request.riskZoneIds().stream()
			.distinct()
			.map(riskZoneId -> AlertZone.builder()
				.id(new AlertZoneId(savedAlert.getId(), riskZoneId))
				.build())
			.toList();
		alertZoneRepository.saveAll(alertZones);

		List<UUID> affectedUsers = getAffectedUsers(savedAlert.getDamId(), request.riskZoneIds());
		String payload = serializePayload(savedAlert);
		List<NotificationOutbox> outboxEntries = new ArrayList<>();
		for (UUID userId : affectedUsers) {
			AlertRecipient recipient = alertRecipientRepository.save(AlertRecipient.builder()
				.alertId(savedAlert.getId())
				.userId(userId)
				.deliveryStatus(DeliveryStatus.PENDING)
				.createdAt(now)
				.build());

			outboxEntries.add(NotificationOutbox.builder()
				.alertId(savedAlert.getId())
				.recipientId(recipient.getId())
				.channel(Channel.PUSH)
				.payload(payload)
				.status(OutboxStatus.PENDING)
				.attemptCount(0)
				.nextAttemptAt(now)
				.createdAt(now)
				.build());
		}
		notificationOutboxRepository.saveAll(outboxEntries);

		return savedAlert;
	}

	private List<UUID> getAffectedUsers(UUID damId, List<UUID> riskZoneIds) {
		return List.of();
	}

	private String serializePayload(Alert alert) {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("alertId", alert.getId());
		details.put("damId", alert.getDamId());
		details.put("severity", alert.getSeverity());
		details.put("title", alert.getTitle());
		details.put("message", alert.getMessage());
		details.put("recommendedAction", alert.getRecommendedAction());
		details.put("evacuationRequired", alert.isEvacuationRequired());
		details.put("expiresAt", alert.getExpiresAt());
		try {
			return objectMapper.writeValueAsString(details);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Unable to serialize the alert notification payload.", exception);
		}
	}
}