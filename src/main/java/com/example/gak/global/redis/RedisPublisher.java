package com.example.gak.global.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

	private final StringRedisTemplate redisTemplate;

	public void waitingRoomPublish(
		Long sessionId
	) {
		String channel = "waiting/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}
}