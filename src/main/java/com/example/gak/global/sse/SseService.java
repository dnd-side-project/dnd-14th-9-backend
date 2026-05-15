package com.example.gak.global.sse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseService {

	private final PresenceService presenceService;

	private final Map<Long, List<SseEmitter>> waitingEmitters = new ConcurrentHashMap<>();
	private final Map<Long, List<SseEmitter>> inProgressEmitters = new ConcurrentHashMap<>();
	private final Map<Long, List<SseEmitter>> sessionStatusEmitters = new ConcurrentHashMap<>();
	private final Map<Long, List<SseEmitter>> reactionEmitters = new ConcurrentHashMap<>();
	private final Map<String, List<SseEmitter>> memberReactionEmitters = new ConcurrentHashMap<>();

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

	public void sendToWaitingEmitter(SseEmitter emitter, Object data) throws IOException {
		emitter.send(SseEmitter.event()
			.name("waiting-members-updated")
			.data(data));
	}

	private void removeWaitingEmitter(Long sessionId, SseEmitter emitter) {
		List<SseEmitter> emitters = waitingEmitters.get(sessionId);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}

	public SseEmitter subscribeInProgress(Long sessionId) {
		SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

		inProgressEmitters
			.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		emitter.onCompletion(() -> removeInProgressEmitter(sessionId, emitter));
		emitter.onTimeout(() -> removeInProgressEmitter(sessionId, emitter));
		emitter.onError(e -> removeInProgressEmitter(sessionId, emitter));

		return emitter;
	}

	public void sendInProgress(Long sessionId, Object data) {
		List<SseEmitter> emitters = inProgressEmitters.get(sessionId);
		if (emitters == null)
			return;

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("in-progress-members-updated")
					.data(data)
				);
			} catch (IOException e) {
				removeInProgressEmitter(sessionId, emitter);
			}
		}
	}

	public void sendToInProgressEmitter(SseEmitter emitter, Object data) throws IOException {
		emitter.send(SseEmitter.event()
			.name("in-progress-members-updated")
			.data(data));
	}

	private void removeInProgressEmitter(Long sessionId, SseEmitter emitter) {
		List<SseEmitter> emitters = inProgressEmitters.get(sessionId);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}

	public SseEmitter subscribeSessionStatus(Long sessionId, Long memberId) {
		SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

		sessionStatusEmitters
			.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		if (memberId != null) {
			presenceService.onConnect(sessionId, memberId);

			Runnable onDisconnect = () -> {
				removeSessionStatusEmitter(sessionId, emitter);
				presenceService.onDisconnect(sessionId, memberId);
			};
			emitter.onCompletion(onDisconnect);
			emitter.onTimeout(onDisconnect);
			emitter.onError(e -> onDisconnect.run());
		} else {
			emitter.onCompletion(() -> removeSessionStatusEmitter(sessionId, emitter));
			emitter.onTimeout(() -> removeSessionStatusEmitter(sessionId, emitter));
			emitter.onError(e -> removeSessionStatusEmitter(sessionId, emitter));
		}

		return emitter;
	}

	public void sendSessionStatus(Long sessionId, Object data) {
		List<SseEmitter> emitters = sessionStatusEmitters.get(sessionId);
		if (emitters == null)
			return;

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("session-status-updated")
					.data(data)
				);
			} catch (IOException e) {
				removeSessionStatusEmitter(sessionId, emitter);
			}
		}
	}

	public void sendToSessionStatusEmitter(SseEmitter emitter, Object data) throws IOException {
		emitter.send(SseEmitter.event()
			.name("session-status-updated")
			.data(data));
	}

	private void removeSessionStatusEmitter(Long sessionId, SseEmitter emitter) {
		List<SseEmitter> emitters = sessionStatusEmitters.get(sessionId);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}

	public SseEmitter subscribeReaction(Long sessionId) {
		SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

		reactionEmitters
			.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		emitter.onCompletion(() -> removeReactionEmitter(sessionId, emitter));
		emitter.onTimeout(() -> removeReactionEmitter(sessionId, emitter));
		emitter.onError(e -> removeReactionEmitter(sessionId, emitter));

		return emitter;
	}

	public void sendReaction(Long sessionId, Object data) {
		List<SseEmitter> emitters = reactionEmitters.get(sessionId);
		if (emitters == null)
			return;

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("reaction-updated")
					.data(data)
				);
			} catch (IOException e) {
				removeReactionEmitter(sessionId, emitter);
			}
		}
	}

	public void sendToReactionEmitter(SseEmitter emitter, Object data) throws IOException {
		emitter.send(SseEmitter.event()
			.name("reaction-updated")
			.data(data));
	}

	private void removeReactionEmitter(Long sessionId, SseEmitter emitter) {
		List<SseEmitter> emitters = reactionEmitters.get(sessionId);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}

	public SseEmitter subscribeMemberReaction(Long sessionId, Long memberId) {
		String key = sessionId + ":" + memberId;
		SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

		memberReactionEmitters
			.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		emitter.onCompletion(() -> removeMemberReactionEmitter(key, emitter));
		emitter.onTimeout(() -> removeMemberReactionEmitter(key, emitter));
		emitter.onError(e -> removeMemberReactionEmitter(key, emitter));

		return emitter;
	}

	public void sendMemberReaction(Long sessionId, Long memberId, Object data) {
		String key = sessionId + ":" + memberId;
		List<SseEmitter> emitters = memberReactionEmitters.get(key);
		if (emitters == null)
			return;

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event()
					.name("member-reaction-updated")
					.data(data)
				);
			} catch (IOException e) {
				removeMemberReactionEmitter(key, emitter);
			}
		}
	}

	private void removeMemberReactionEmitter(String key, SseEmitter emitter) {
		List<SseEmitter> emitters = memberReactionEmitters.get(key);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}

	@Scheduled(fixedDelay = 5000)
	public void sendHeartbeat() {
		for (Map.Entry<Long, List<SseEmitter>> entry : sessionStatusEmitters.entrySet()) {
			List<SseEmitter> emitters = entry.getValue();
			for (SseEmitter emitter : emitters) {
				try {
					emitter.send(SseEmitter.event().comment("heartbeat"));
				} catch (IOException e) {
					emitter.completeWithError(e);
				}
			}
		}
	}
}