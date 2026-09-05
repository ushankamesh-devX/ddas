package com.damalert.notification.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.damalert.alert.entity.AlertRecipient;
import com.damalert.alert.entity.DeliveryStatus;
import com.damalert.alert.repository.AlertRecipientRepository;
import com.damalert.notification.entity.NotificationOutbox;
import com.damalert.notification.entity.OutboxStatus;
import com.damalert.notification.repository.NotificationOutboxRepository;

import lombok.RequiredArgsConstructor;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class OutboxProcessor {

	private static final int BATCH_SIZE = 100;
	private static final int MAX_RETRIES = 5;
	private static final int BASE_BACKOFF_SECONDS = 5;

	private final NotificationOutboxRepository outboxRepository;
	private final AlertRecipientRepository recipientRepository;
	private final PushNotificationSender pushNotificationSender;
	@Lazy
	private final OutboxProcessor transactionalProcessor;

	@Scheduled(fixedDelay = 5000)
	public void processOutbox() {
		List<NotificationOutbox> messages = outboxRepository.findReadyForProcessing(BATCH_SIZE);
		for (NotificationOutbox message : messages) {
			try {
				transactionalProcessor.processMessage(message.getId());
			}
			catch (RuntimeException exception) {
				// Keep processing the remaining batch when one message cannot be handled.
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processMessage(UUID messageId) {
		NotificationOutbox message = outboxRepository.findById(messageId).orElse(null);
		if (message == null || message.getStatus() == OutboxStatus.SENT || message.getStatus() == OutboxStatus.DEAD) {
			return;
		}

		String pushToken = findActivePushToken(message.getRecipientId());
		boolean sent;
		try {
			sent = pushToken != null && pushNotificationSender.sendPush(pushToken, message.getPayload());
		}
		catch (RuntimeException exception) {
			sent = false;
		}
		if (sent) {
			markSent(message);
			return;
		}

		markFailed(message);
	}

	private void markSent(NotificationOutbox message) {
		OffsetDateTime now = OffsetDateTime.now();
		message.setStatus(OutboxStatus.SENT);
		message.setProcessedAt(now);
		outboxRepository.save(message);

		if (message.getRecipientId() != null) {
			recipientRepository.findById(message.getRecipientId()).ifPresent(recipient -> {
				recipient.setDeliveryStatus(DeliveryStatus.SENT);
				recipient.setSentAt(now);
				recipientRepository.save(recipient);
			});
		}
	}

	private void markFailed(NotificationOutbox message) {
		int attempts = message.getAttemptCount() + 1;
		message.setAttemptCount(attempts);
		if (attempts >= MAX_RETRIES) {
			message.setStatus(OutboxStatus.DEAD);
			message.setProcessedAt(OffsetDateTime.now());
		}
		else {
			message.setStatus(OutboxStatus.FAILED);
			long delaySeconds = (long) BASE_BACKOFF_SECONDS << (attempts - 1);
			message.setNextAttemptAt(OffsetDateTime.now().plusSeconds(delaySeconds));
		}
		outboxRepository.save(message);
	}

	private String findActivePushToken(UUID recipientId) {
		return recipientId == null ? null : "mock-push-token-" + recipientId;
	}
}