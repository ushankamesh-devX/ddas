package com.damalert.ddas.monitoring.api;

public record MqttConnectionResponse(String host, int port, String topic) {
}
