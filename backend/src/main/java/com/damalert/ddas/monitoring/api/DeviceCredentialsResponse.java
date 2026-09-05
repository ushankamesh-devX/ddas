package com.damalert.ddas.monitoring.api;

public record DeviceCredentialsResponse(String deviceId, String deviceKey, boolean shownOnce) {
}
