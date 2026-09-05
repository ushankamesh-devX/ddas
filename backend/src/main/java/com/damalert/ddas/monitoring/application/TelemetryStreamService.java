package com.damalert.ddas.monitoring.application;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Profile("!standalone")
public class TelemetryStreamService {
	private final ConcurrentHashMap<UUID, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public SseEmitter subscribe(UUID damId) {
		SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
		Set<SseEmitter> damEmitters = emitters.computeIfAbsent(damId, ignored -> ConcurrentHashMap.newKeySet());
		damEmitters.add(emitter);
		Runnable remove = () -> remove(damId, emitter);
		emitter.onCompletion(remove);
		emitter.onTimeout(remove);
		emitter.onError(ignored -> remove.run());
		try {
			emitter.send(SseEmitter.event().name("connected").data(java.util.Map.of("damId", damId)));
		}
		catch (IOException ex) {
			remove.run();
		}
		return emitter;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void broadcast(TelemetryEvent event) {
		Set<SseEmitter> damEmitters = emitters.getOrDefault(event.damId(), Set.of());
		for (SseEmitter emitter : damEmitters) {
			try {
				emitter.send(SseEmitter.event().name("telemetry").id(UUID.randomUUID().toString()).data(event.reading()));
			}
			catch (IOException | IllegalStateException ex) {
				remove(event.damId(), emitter);
			}
		}
	}

	private void remove(UUID damId, SseEmitter emitter) {
		Set<SseEmitter> damEmitters = emitters.get(damId);
		if (damEmitters == null) return;
		damEmitters.remove(emitter);
		if (damEmitters.isEmpty()) emitters.remove(damId, damEmitters);
	}
}
