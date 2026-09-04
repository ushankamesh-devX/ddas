package com.damalert.ddas.common.error;

import java.time.Instant;
import java.util.Map;

public record ApiError(
	String code,
	String message,
	Map<String, Object> details,
	String requestId,
	Instant timestamp
) {
}
