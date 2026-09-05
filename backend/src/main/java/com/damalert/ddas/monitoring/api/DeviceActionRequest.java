package com.damalert.ddas.monitoring.api;

import jakarta.validation.constraints.Size;

public record DeviceActionRequest(@Size(max = 500) String reason) {
}
