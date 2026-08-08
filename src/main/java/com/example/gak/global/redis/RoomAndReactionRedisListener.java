package com.example.gak.global.redis;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.dto.enums.EventType;
import com.example.gak.domain.session.service.ReactionSummaryQueryService;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.sse.ReactionEventService;
import com.example.gak.global.sse.RoomEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomAndReactionRedisListener implements MessageListener {

	private final SessionQueryService sessionQueryService;
	private final ReactionSummaryQueryService reactionSummaryQueryService;
	private final RoomEventService roomEventService;
	private final ReactionEventService reactionEventService;
	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel());
		String[] parts = channel.split("/");
		String type = parts[0];

		if ("member".equals(type)) {
			Long sessionId = Long.parseLong(parts[3]);
			handleReaction(sessionId);
		} else if ("kicked".equals(type)) {
			Long sessionId = Long.parseLong(parts[1]);
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			handleKicked(sessionId, json);
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
			default -> log.warn("[room-reaction-listener] Unknown channel type: {}", type);
		}
	}

	private void handleWaiting(Long sessionId) {
		SessionResponseDTO.WaitingResponseDTO waitingResponseDTO =
			sessionQueryService.getCurrentWaitingRoom(sessionId);

		SessionResponseDTO.SessionWaitingRoomResponseDTO<SessionResponseDTO.WaitingResponseDTO> dto =
			SessionResponseDTO.SessionWaitingRoomResponseDTO.<SessionResponseDTO.WaitingResponseDTO>builder()
				.eventType(EventType.ROOM_UPDATE)
				.data(waitingResponseDTO)
				.build();

		roomEventService.sendWaitingUpdate(sessionId, dto);
	}

	private void handleKicked(Long sessionId, String json) {
		try {
			List<Long> memberIds = objectMapper.readValue(json, new TypeReference<List<Long>>() {
			});

			SessionResponseDTO.SessionWaitingRoomResponseDTO<SessionResponseDTO.KickedUserResponseDTO> dto =
				SessionResponseDTO.SessionWaitingRoomResponseDTO.<SessionResponseDTO.KickedUserResponseDTO>builder()
					.eventType(EventType.KICKED)
					.data(SessionConverter.toKickedUserResponseDTO(memberIds))
					.build();

			roomEventService.sendWaitingUpdate(sessionId, dto);
		} catch (JsonProcessingException e) {
			log.error("[room-reaction-listener] 강퇴 이벤트 메시지 역직렬화 실패", e);
		}
	}

	private void handleInProgress(Long sessionId) {
		roomEventService.sendInProgressUpdate(sessionId, sessionQueryService.getCurrentSessionRoom(sessionId));
	}

	private void handleSessionStatus(Long sessionId) {
		roomEventService.sendSessionStatusUpdate(sessionId, sessionQueryService.getSessionStatus(sessionId));
	}

	private void handleReaction(Long sessionId) {
		// DB 조회는 여기서 한 번만 하고, 구독자별 개인화는 ReactionEventService 안에서 메모리 조회로 끝낸다.
		ReactionSummaryQueryService.ReactionSnapshot snapshot = reactionSummaryQueryService.getReactionSnapshot(
			sessionId);
		reactionEventService.sendReactionUpdate(sessionId,
			memberId -> reactionSummaryQueryService.toPersonalized(snapshot, memberId));
	}
}
