package com.example.gak.global.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

	private final StringRedisTemplate redisTemplate;

	public void waitingRoomPublish(Long sessionId) {
		String channel = "waiting/" + sessionId;
		log.info("[Redis Publish] 시작 - 채널: {}", channel);

		try {
			redisTemplate.convertAndSend(channel, "UPDATE");
			log.info("[Redis Publish] 성공 - 채널: {}", channel);
		} catch (Exception e) {
			log.error("[Redis Publish] 실패 - 채널: {}, 에러: {}", channel, e.getMessage(), e);
		}

		log.info("[Redis Publish] 종료 - 채널: {}", channel);
	}

	public void inProgressRoomPublish(
		Long sessionId
	) {
		String channel = "in-progress/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}
}