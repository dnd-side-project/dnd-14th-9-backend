package com.example.gak.global.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory connectionFactory,
		RedisSubscriber redisSubscriber
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();

		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(redisSubscriber, new PatternTopic("waiting/*"));
		container.addMessageListener(redisSubscriber, new PatternTopic("in-progress/*"));

		return container;
	}
}
