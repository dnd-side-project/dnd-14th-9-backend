package com.example.gak.global.sse;

import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.domain.session.service.SessionCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService implements MessageListener {

	static final long GRACE_PERIOD_SECONDS = 10;
	static final String GRACE_PREFIX = "grace:";
	static final String CONN_PREFIX = "presence:conn:";
	static final long CONN_TTL_SECONDS = 60 * 60 + 60;

	private static final RedisScript<Long> CONNECT_SCRIPT = RedisScript.of("""
		redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
		return redis.call('DEL', KEYS[2])
		""", Long.class);

	private static final RedisScript<Long> DISCONNECT_SCRIPT = RedisScript.of("""
		local current = redis.call('GET', KEYS[1])
		if current == false or current == ARGV[1] then
		  redis.call('DEL', KEYS[1])
		  redis.call('SET', KEYS[2], '1', 'EX', ARGV[2])
		  return 1
		end
		return 0
		""", Long.class);

	private final StringRedisTemplate redisTemplate;
	private final SessionCommandService sessionCommandService;
	private final SessionRoomMemberRepository sessionRoomMemberRepository;

	public static String newConnectionId() {
		return UUID.randomUUID().toString();
	}

	public void onConnect(Long sessionId, Long memberId, String connId) {
		Long cancelled = redisTemplate.execute(CONNECT_SCRIPT,
			List.of(connKey(sessionId, memberId), graceKey(sessionId, memberId)),
			connId, String.valueOf(CONN_TTL_SECONDS));
		if (cancelled != null && cancelled > 0) {
			log.info("presence 재연결 — 퇴장 타이머 취소: session={}, member={}, conn={}", sessionId, memberId, connId);
		}
	}

	public void onDisconnect(Long sessionId, Long memberId, String connId) {
		if (!sessionRoomMemberRepository.existsBySessionRoomIdAndMemberId(sessionId, memberId)) {
			return;
		}
		Long started = redisTemplate.execute(DISCONNECT_SCRIPT,
			List.of(connKey(sessionId, memberId), graceKey(sessionId, memberId)),
			connId, String.valueOf(GRACE_PERIOD_SECONDS));
		if (started != null && started == 1L) {
			log.info("presence 연결 끊김 — 유예 타이머 시작 ({}초): session={}, member={}, conn={}",
				GRACE_PERIOD_SECONDS, sessionId, memberId, connId);
		} else {
			log.info("presence 연결 끊김 무시 (다른 연결로 이미 대체됨): session={}, member={}, conn={}",
				sessionId, memberId, connId);
		}
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String expiredKey = new String(message.getBody());
		if (!expiredKey.startsWith(GRACE_PREFIX)) {
			return;
		}

		String[] parts = expiredKey.substring(GRACE_PREFIX.length()).split(":");
		if (parts.length != 2) {
			return;
		}

		try {
			Long sessionId = Long.parseLong(parts[0]);
			Long memberId = Long.parseLong(parts[1]);
			if (Boolean.TRUE.equals(redisTemplate.hasKey(connKey(sessionId, memberId)))) {
				log.info("presence 유예 만료됐지만 현재 연결 존재 — 퇴장 생략: session={}, member={}", sessionId, memberId);
				return;
			}
			log.info("presence 유예 시간 만료 — 자동 퇴장 처리: session={}, member={}", sessionId, memberId);
			sessionCommandService.leaveSessionOnDisconnect(sessionId, memberId);
		} catch (NumberFormatException e) {
			log.warn("grace 키 형식 오류: {}", expiredKey);
		} catch (Exception e) {
			log.warn("presence 자동 퇴장 처리 실패: key={}", expiredKey, e);
		}
	}

	static String connKey(Long sessionId, Long memberId) {
		return CONN_PREFIX + sessionId + ":" + memberId;
	}

	static String graceKey(Long sessionId, Long memberId) {
		return GRACE_PREFIX + sessionId + ":" + memberId;
	}
}
