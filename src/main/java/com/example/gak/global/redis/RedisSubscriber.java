package com.example.gak.global.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.sse.SseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

	private final SessionQueryService sessionQueryService;
	private final SseService sseService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel());

		String[] parts = channel.split("/");
		String type = parts[0];
		Long sessionId = Long.parseLong(parts[1]);

		handleMessage(type, sessionId);
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
}