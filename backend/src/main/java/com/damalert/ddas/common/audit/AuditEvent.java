package com.damalert.ddas.common.audit;

import java.util.Map;
import java.util.UUID;

public record AuditEvent(
	UUID damId,
	UUID actorUserId,
	String action,
	String entityType,
	UUID entityId,
	Map<String, Object> oldValue,
	Map<String, Object> newValue
) {
}
