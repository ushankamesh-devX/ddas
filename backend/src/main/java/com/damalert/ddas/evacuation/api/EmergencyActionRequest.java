package com.damalert.ddas.evacuation.api;

import jakarta.validation.constraints.Size;

public record EmergencyActionRequest(
	@Size(max = 2000) String reason
) {
}
