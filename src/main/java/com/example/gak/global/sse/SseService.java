package com.example.gak.global.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseService {

	private final Map<Long, List<SseEmitter>> waitingEmitters = new ConcurrentHashMap<>();

	public SseEmitter subscribeWaiting(Long sessionId) {
		SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

		waitingEmitters
			.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		emitter.onCompletion(() -> removeWaitingEmitter(sessionId, emitter));
		emitter.onTimeout(() -> removeWaitingEmitter(sessionId, emitter));
		emitter.onError(e -> removeWaitingEmitter(sessionId, emitter));

		return emitter;
	}

	public void sendWaiting(Long sessionId, Object data) {
		List<SseEmitter> emitters = waitingEmitters.get(sessionId);
		if (emitters == null)
			return;

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("waiting-members-updated")
					.data(data)
				);
			} catch (IOException e) {
				removeWaitingEmitter(sessionId, emitter);
			}
		}
	}

	private void removeWaitingEmitter(Long sessionId, SseEmitter emitter) {
		List<SseEmitter> emitters = waitingEmitters.get(sessionId);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}
}