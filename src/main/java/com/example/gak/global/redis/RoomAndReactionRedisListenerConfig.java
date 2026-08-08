package com.example.gak.global.redis;

import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomAndReactionRedisListenerConfig {

	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final RoomAndReactionRedisListener roomAndReactionRedisListener;

	@PostConstruct
	public void registerListener() {
		redisMessageListenerContainer.addMessageListener(roomAndReactionRedisListener, new PatternTopic("waiting/*"));
		redisMessageListenerContainer.addMessageListener(roomAndReactionRedisListener, new PatternTopic("kicked/*"));
		redisMessageListenerContainer.addMessageListener(roomAndReactionRedisListener,
			new PatternTopic("in-progress/*"));
		redisMessageListenerContainer.addMessageListener(roomAndReactionRedisListener, new PatternTopic("session/*"));
		redisMessageListenerContainer.addMessageListener(roomAndReactionRedisListener, new PatternTopic("reaction/*"));
		redisMessageListenerContainer.addMessageListener(roomAndReactionRedisListener,
			new PatternTopic("member/*/reaction/*"));
	}
}
