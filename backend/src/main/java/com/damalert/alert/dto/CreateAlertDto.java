package com.damalert.alert.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.damalert.alert.entity.AlertSeverity;

public record CreateAlertDto(
	UUID damId,
	AlertSeverity severity,
	String title,
	String message,
	String recommendedAction,
	boolean evacuationRequired,
	OffsetDateTime expiresAt,
	String idempotencyKey,
	List<UUID> riskZoneIds
) {
	public CreateAlertDto {
		riskZoneIds = riskZoneIds == null ? List.of() : List.copyOf(riskZoneIds);
	}
}