package com.example.gak.global.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReactionEventService {

	private static final long EMITTER_TIMEOUT_MILLIS = 60 * 60 * 1000L;
	private static final String EVENT_REACTION_SUMMARY_UPDATED = "reaction-updated";

	private record Subscription(SseEmitter emitter, Long memberId) {
	}

	private final Map<Long, List<Subscription>> reactionEmitters = new ConcurrentHashMap<>();

	public SseEmitter subscribeReaction(Long sessionId, Long memberId) {
		SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
		Subscription subscription = new Subscription(emitter, memberId);

		reactionEmitters
			.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
			.add(subscription);

		emitter.onCompletion(() -> removeReactionEmitter(sessionId, emitter));
		emitter.onTimeout(() -> removeReactionEmitter(sessionId, emitter));
		emitter.onError(e -> removeReactionEmitter(sessionId, emitter));

		return emitter;
	}

	public void sendReactionUpdate(Long sessionId, Function<Long, Object> payloadForMember) {
		List<Subscription> subscriptions = reactionEmitters.get(sessionId);
		if (subscriptions == null)
			return;

		for (Subscription subscription : subscriptions) {
			try {
				Object data = payloadForMember.apply(subscription.memberId());
				subscription.emitter().send(SseEmitter.event().name(EVENT_REACTION_SUMMARY_UPDATED).data(data));
			} catch (IOException e) {
				removeReactionEmitter(sessionId, subscription.emitter());
			}
		}
	}

	public void sendToReactionEmitter(SseEmitter emitter, Object data) throws IOException {
		emitter.send(SseEmitter.event().name(EVENT_REACTION_SUMMARY_UPDATED).data(data));
	}

	@Scheduled(fixedDelay = 3000)
	public void sendHeartbeat() {
		for (Map.Entry<Long, List<Subscription>> entry : reactionEmitters.entrySet()) {
			for (Subscription subscription : entry.getValue()) {
				try {
					subscription.emitter().send(SseEmitter.event().comment("heartbeat"));
				} catch (IOException e) {
					subscription.emitter().completeWithError(e);
				}
			}
		}
	}

	private void removeReactionEmitter(Long sessionId, SseEmitter emitter) {
		reactionEmitters.computeIfPresent(sessionId, (id, subscriptions) -> {
			subscriptions.removeIf(s -> s.emitter() == emitter);
			return subscriptions.isEmpty() ? null : subscriptions;
		});
	}
}
