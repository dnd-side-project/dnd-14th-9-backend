package com.example.gak.global.sse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.gak.domain.session.service.SessionCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {

	private static final long GRACE_PERIOD_SECONDS = 30;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingLeaves = new ConcurrentHashMap<>();

	private final SessionCommandService sessionCommandService;

	public void onConnect(Long sessionId, Long memberId) {
		String key = key(sessionId, memberId);
		ScheduledFuture<?> pending = pendingLeaves.remove(key);
		if (pending != null) {
			pending.cancel(false);
			log.info("presence reconnected — cancelled pending leave: session={}, member={}", sessionId, memberId);
		}
	}

	public void onDisconnect(Long sessionId, Long memberId) {
		String key = key(sessionId, memberId);
		ScheduledFuture<?> future = scheduler.schedule(() -> {
			pendingLeaves.remove(key);
			try {
				sessionCommandService.leaveSessionOnDisconnect(sessionId, memberId);
			} catch (Exception e) {
				log.warn("presence leave failed: session={}, member={}", sessionId, memberId, e);
			}
		}, GRACE_PERIOD_SECONDS, TimeUnit.SECONDS);
		pendingLeaves.put(key, future);
		log.info("presence disconnected — grace period started: session={}, member={}", sessionId, memberId);
	}

	private String key(Long sessionId, Long memberId) {
		return sessionId + ":" + memberId;
	}
}
