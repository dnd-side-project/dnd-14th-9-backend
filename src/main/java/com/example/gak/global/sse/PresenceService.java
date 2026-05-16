package com.example.gak.global.sse;

import java.time.Duration;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.domain.session.service.SessionCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService implements MessageListener {

	private static final long GRACE_PERIOD_SECONDS = 10;
	private static final String KEY_PREFIX = "grace:";

	private final StringRedisTemplate redisTemplate;
	private final SessionCommandService sessionCommandService;
	private final SessionRoomMemberRepository sessionRoomMemberRepository;

	public void onConnect(Long sessionId, Long memberId) {
		String key = KEY_PREFIX + sessionId + ":" + memberId;
		Boolean deleted = redisTemplate.delete(key);
		if (Boolean.TRUE.equals(deleted)) {
			log.info("presence 재연결 — 퇴장 타이머 취소: session={}, member={}", sessionId, memberId);
		}
	}

	public void onDisconnect(Long sessionId, Long memberId) {
		if (!sessionRoomMemberRepository.existsBySessionRoomIdAndMemberId(sessionId, memberId)) {
			return;
		}
		String key = KEY_PREFIX + sessionId + ":" + memberId;
		redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(GRACE_PERIOD_SECONDS));
		log.info("presence 연결 끊김 — 유예 타이머 시작 ({}초): session={}, member={}", GRACE_PERIOD_SECONDS, sessionId, memberId);
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String expiredKey = new String(message.getBody());
		if (!expiredKey.startsWith(KEY_PREFIX)) {
			return;
		}

		String[] parts = expiredKey.substring(KEY_PREFIX.length()).split(":");
		if (parts.length != 2) {
			return;
		}

		try {
			Long sessionId = Long.parseLong(parts[0]);
			Long memberId = Long.parseLong(parts[1]);
			log.info("presence 유예 시간 만료 — 자동 퇴장 처리: session={}, member={}", sessionId, memberId);
			sessionCommandService.leaveSessionOnDisconnect(sessionId, memberId);
		} catch (NumberFormatException e) {
			log.warn("grace 키 형식 오류: {}", expiredKey);
		} catch (Exception e) {
			log.warn("presence 자동 퇴장 처리 실패: key={}", expiredKey, e);
		}
	}
}
