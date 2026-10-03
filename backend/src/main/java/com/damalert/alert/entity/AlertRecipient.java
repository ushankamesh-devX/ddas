package com.damalert.alert.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import lombok.*;

@Entity @Table(name = "alert_recipient")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AlertRecipient {
	@Id @GeneratedValue @UuidGenerator private UUID id;
	@Column(name = "alert_id", nullable = false) private UUID alertId;
	@Column(name = "user_id", nullable = false) private UUID userId;
	@Enumerated(EnumType.STRING) @Column(name = "delivery_status", nullable = false, length = 24) @Builder.Default private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;
	@Column(name = "first_attempt_at") private OffsetDateTime firstAttemptAt;
	@Column(name = "sent_at") private OffsetDateTime sentAt;
	@Column(name = "opened_at") private OffsetDateTime openedAt;
	@Column(name = "acknowledged_at") private OffsetDateTime acknowledgedAt;
	@Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
}