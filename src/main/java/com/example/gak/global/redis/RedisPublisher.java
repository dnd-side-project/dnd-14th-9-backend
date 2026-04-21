package com.example.gak.global.redis;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.gak.domain.chat.dto.ChatResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

	private final StringRedisTemplate redisTemplate;
	private final RedisTemplate<String, Object> chatRedisTemplate;
	private final ObjectMapper objectMapper;

	public void waitingRoomPublish(
		Long sessionId
	) {
		String channel = "waiting/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}

	public void kickedPublish(
		Long sessionId,
		List<Long> memberIds
	) {
		String channel = "kicked/" + sessionId;
		try {
			String message = objectMapper.writeValueAsString(memberIds);
			redisTemplate.convertAndSend(channel, message);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void inProgressRoomPublish(
		Long sessionId
	) {
		String channel = "in-progress/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}

	public void sessionStatusPublish(
		Long sessionId
	) {
		String channel = "session/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}

	public void reactionPublish(
		Long sessionId
	) {
		String channel = "reaction/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}

	public void memberReactionPublish(
		Long sessionId,
		Long memberId
	) {
		String channel = "member/" + memberId + "/reaction/" + sessionId;
		redisTemplate.convertAndSend(channel, "UPDATE");
	}

	public void chatMessagePublish(
		Long sessionId,
		ChatResponseDTO.ChatMessageResponseDTO dto
	) {
		String channel = "chat/" + sessionId;
		chatRedisTemplate.convertAndSend(channel, dto);
	}
}