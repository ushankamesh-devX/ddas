package com.damalert.ddas.monitoring.mqtt;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.damalert.ddas.monitoring.application.TelemetryIngestionService;
import com.damalert.ddas.monitoring.application.TelemetryPayload;

import tools.jackson.databind.ObjectMapper;

@Component
@Profile("!standalone")
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true")
public class MqttTelemetrySubscriber implements SmartLifecycle {
	private static final Logger log = LoggerFactory.getLogger(MqttTelemetrySubscriber.class);
	private static final String TOPIC = "dams/+/devices/+/telemetry";

	private final TelemetryIngestionService ingestion;
	private final ObjectMapper objectMapper;
	private final String brokerUri;
	private final String username;
	private final String password;
	private volatile MqttClient client;
	private volatile boolean running;

	public MqttTelemetrySubscriber(TelemetryIngestionService ingestion, ObjectMapper objectMapper,
		@Value("${app.mqtt.broker-uri:tcp://localhost:1883}") String brokerUri,
		@Value("${app.mqtt.username:}") String username,
		@Value("${app.mqtt.password:}") String password) {
		this.ingestion = ingestion;
		this.objectMapper = objectMapper;
		this.brokerUri = brokerUri;
		this.username = username;
		this.password = password;
	}

	@Override
	public synchronized void start() {
		if (running) return;
		try {
			client = new MqttClient(brokerUri, "ddas-backend-" + UUID.randomUUID(), new MemoryPersistence());
			client.setCallback(new Callback());
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(true);
			options.setCleanSession(false);
			options.setConnectionTimeout(10);
			if (!username.isBlank()) options.setUserName(username);
			if (!password.isBlank()) options.setPassword(password.toCharArray());
			client.connect(options);
			client.subscribe(TOPIC, 1);
			running = true;
			log.info("MQTT telemetry subscriber connected to {}", brokerUri);
		}
		catch (Exception ex) {
			log.error("Unable to start MQTT telemetry subscriber", ex);
			stop();
		}
	}

	@Override
	public synchronized void stop() {
		running = false;
		if (client == null) return;
		try {
			if (client.isConnected()) client.disconnect();
			client.close();
		}
		catch (Exception ex) {
			log.warn("Error while stopping MQTT telemetry subscriber", ex);
		}
		finally {
			client = null;
		}
	}

	@Override
	public boolean isRunning() { return running; }

	@Override
	public int getPhase() { return Integer.MAX_VALUE - 100; }

	private final class Callback implements MqttCallbackExtended {
		@Override
		public void connectComplete(boolean reconnect, String serverURI) {
			if (reconnect && client != null) {
				try {
					client.subscribe(TOPIC, 1);
				}
				catch (Exception ex) {
					log.error("Unable to restore MQTT telemetry subscription", ex);
				}
			}
		}

		@Override
		public void connectionLost(Throwable cause) {
			log.warn("MQTT telemetry connection lost; automatic reconnect is enabled", cause);
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) {
			String raw = new String(message.getPayload(), StandardCharsets.UTF_8);
			try {
				TelemetryPayload payload = objectMapper.readValue(raw, TelemetryPayload.class);
				ingestion.ingest(topic, payload, raw);
			}
			catch (Exception ex) {
				log.warn("Rejected MQTT telemetry on topic {}: {}", topic, ex.getMessage());
			}
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) { }
	}
}
