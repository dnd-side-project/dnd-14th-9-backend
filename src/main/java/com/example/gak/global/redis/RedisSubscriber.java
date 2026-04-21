package com.example.gak.global.redis;

import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.dto.enums.EventType;
import com.example.gak.domain.session.service.SessionCommandService;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.sse.SseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

	private final SessionQueryService sessionQueryService;
	private final SseService sseService;
	private final SimpMessagingTemplate messagingTemplate;
	private final StringRedisTemplate redisTemplate;
	private final SessionCommandService sessionCommandService;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel());
		String[] parts = channel.split("/");

		String type = parts[0];

		if ("member".equals(type)) {
			Long memberId = Long.parseLong(parts[1]);
			Long sessionId = Long.parseLong(parts[3]);
			handleMemberReaction(sessionId, memberId);
		} else if ("chat".equals(type)) {
			Long sessionId = Long.parseLong(parts[1]);
			String json = new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
			handleChatMessage(sessionId, json);
		} else if ("kicked".equals(type)) {
			Long sessionId = Long.parseLong(parts[1]);
			log.info("channel={}", channel);
			String json = new String(message.getBody(), java.nio.charset.StandardCharsets.UTF_8);
			log.info("kicked payload={}", json);
			try {
				handleKicked(sessionId, json);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			Long sessionId = Long.parseLong(parts[1]);
			handleMessage(type, sessionId);
		}
	}

	private void handleMessage(String type, Long sessionId) {
		switch (type) {
			case "waiting" -> handleWaiting(sessionId);
			case "in-progress" -> handleInProgress(sessionId);
			case "session" -> handleSessionStatus(sessionId);
			case "reaction" -> handleReaction(sessionId);
			default -> log.warn("Unknown channel type: {}", type);
		}
	}

	private void handleWaiting(Long sessionId) {
		SessionResponseDTO.WaitingResponseDTO dto =
			sessionQueryService.getCurrentWaitingRoom(sessionId);
		sseService.sendWaiting(sessionId, dto);
	}

	private void handleKicked(Long sessionId, String json) throws JsonProcessingException {
		List<Long> memberIds = objectMapper.readValue(
			json,
			new TypeReference<List<Long>>() {
			}
		);
		SessionResponseDTO.SessionWaitingRoomResponseDTO<SessionResponseDTO.KickedUserResponseDTO> dto =
			getKickedUserInfo(memberIds);
		sseService.sendWaiting(sessionId, dto);
	}

	private void handleInProgress(Long sessionId) {
		SessionResponseDTO.InProgressResponseDTO dto =
			sessionQueryService.getCurrentSessionRoom(sessionId);
		sseService.sendInProgress(sessionId, dto);
	}

	private void handleSessionStatus(Long sessionId) {
		SessionResponseDTO.SessionStartResponseDTO dto =
			sessionQueryService.getSessionStatus(sessionId);
		sseService.sendSessionStatus(sessionId, dto);
	}

	private void handleReaction(Long sessionId) {
		SessionResponseDTO.EmojiResultResponseDTO dto =
			sessionQueryService.getReactionStatus(sessionId);
		sseService.sendReaction(sessionId, dto);
	}

	private void handleMemberReaction(Long sessionId, Long memberId) {
		SessionResponseDTO.EmojiResultResponseDTO dto =
			sessionQueryService.getMemberReactionStatus(sessionId, memberId);
		sseService.sendMemberReaction(sessionId, memberId, dto);
	}

	private void handleChatMessage(Long sessionId, String json) {
		messagingTemplate.convertAndSend("/sub/chat/" + sessionId, json);
	}

	private SessionResponseDTO.SessionWaitingRoomResponseDTO<SessionResponseDTO.KickedUserResponseDTO> getKickedUserInfo(
		List<Long> memberIds) {
		SessionResponseDTO.KickedUserResponseDTO kickedUserResponseDTO
			= SessionConverter.toKickedUserResponseDTO(memberIds);

		return SessionResponseDTO.SessionWaitingRoomResponseDTO.<SessionResponseDTO.KickedUserResponseDTO>builder()
			.eventType(EventType.KICKED)
			.data(kickedUserResponseDTO)
			.build();
	}
}