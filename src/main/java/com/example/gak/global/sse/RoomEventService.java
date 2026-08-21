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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomEventService {

	private static final long EMITTER_TIMEOUT_MILLIS = 60 * 60 * 1000L;

	public static final String EVENT_WAITING_UPDATED = "waiting-members-updated";
	public static final String EVENT_IN_PROGRESS_UPDATED = "in-progress-members-updated";
	public static final String EVENT_SESSION_STATUS_UPDATED = "session-status-updated";

	private final PresenceService presenceService;

	private final Map<Long, List<SseEmitter>> roomEmitters = new ConcurrentHashMap<>();

	private final Map<String, SseEmitter> latestEmitterByMember = new ConcurrentHashMap<>();

	public SseEmitter subscribeRoom(Long sessionId, Long memberId) {
		SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);

		roomEmitters
			.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>())
			.add(emitter);

		if (memberId != null) {
			String memberKey = sessionId + ":" + memberId;

			SseEmitter previous = latestEmitterByMember.put(memberKey, emitter);
			if (previous != null && previous != emitter) {
				log.info("[room-event] 동일 member의 기존 emitter를 새 연결로 대체: session={}, member={}", sessionId, memberId);
				removeRoomEmitter(sessionId, previous);
				previous.complete();
			}

			presenceService.onConnect(sessionId, memberId);

			Runnable onDisconnect = () -> {
				removeRoomEmitter(sessionId, emitter);
				boolean wasCurrent = latestEmitterByMember.remove(memberKey, emitter);
				if (!wasCurrent) {
					return;
				}
				presenceService.onDisconnect(sessionId, memberId);
			};
			emitter.onCompletion(onDisconnect);
			emitter.onTimeout(onDisconnect);
			emitter.onError(e -> onDisconnect.run());
		} else {
			emitter.onCompletion(() -> removeRoomEmitter(sessionId, emitter));
			emitter.onTimeout(() -> removeRoomEmitter(sessionId, emitter));
			emitter.onError(e -> removeRoomEmitter(sessionId, emitter));
		}

		return emitter;
	}

	public void sendWaitingUpdate(Long sessionId, Object data) {
		broadcast(sessionId, EVENT_WAITING_UPDATED, data);
	}

	public void sendInProgressUpdate(Long sessionId, Object data) {
		broadcast(sessionId, EVENT_IN_PROGRESS_UPDATED, data);
	}

	public void sendSessionStatusUpdate(Long sessionId, Object data) {
		broadcast(sessionId, EVENT_SESSION_STATUS_UPDATED, data);
	}

	public void sendToRoomEmitter(SseEmitter emitter, String eventName, Object data) throws IOException {
		emitter.send(SseEmitter.event().name(eventName).data(data));
	}

	private void broadcast(Long sessionId, String eventName, Object data) {
		List<SseEmitter> emitters = roomEmitters.get(sessionId);
		if (emitters == null)
			return;

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(data));
			} catch (IOException e) {
				removeRoomEmitter(sessionId, emitter);
			}
		}
	}

	private void removeRoomEmitter(Long sessionId, SseEmitter emitter) {
		roomEmitters.computeIfPresent(sessionId, (id, emitters) -> {
			emitters.remove(emitter);
			return emitters.isEmpty() ? null : emitters;
		});
	}

	@Scheduled(fixedDelay = 3000)
	public void sendHeartbeat() {
		for (Map.Entry<Long, List<SseEmitter>> entry : roomEmitters.entrySet()) {
			for (SseEmitter emitter : entry.getValue()) {
				try {
					emitter.send(SseEmitter.event().comment("heartbeat"));
				} catch (IOException e) {
					emitter.completeWithError(e);
				}
			}
		}
	}
}
