package com.example.gak.global.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> ChatRedisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

		return template;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		RedisConnectionFactory connectionFactory,
		RedisSubscriber redisSubscriber
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();

		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(redisSubscriber, new PatternTopic("waiting/*"));
		container.addMessageListener(redisSubscriber, new PatternTopic("kicked/*"));
		container.addMessageListener(redisSubscriber, new PatternTopic("in-progress/*"));
		container.addMessageListener(redisSubscriber, new PatternTopic("session/*"));
		container.addMessageListener(redisSubscriber, new PatternTopic("reaction/*"));
		container.addMessageListener(redisSubscriber, new PatternTopic("member/*/reaction/*"));

		container.addMessageListener(redisSubscriber, new PatternTopic("chat/*"));

		return container;
	}
}
