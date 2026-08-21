package com.example.gak.domain.session.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.dto.enums.EventType;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.service.ReactionSummaryQueryService;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.security.oauth2.CustomOAuth2User;
import com.example.gak.global.sse.ReactionEventService;
import com.example.gak.global.sse.RoomEventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "세션 SSE (통합 채널)")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class RoomEventSseController {

	private final RoomEventService roomEventService;
	private final ReactionEventService reactionEventService;
	private final SessionQueryService sessionQueryService;
	private final ReactionSummaryQueryService reactionSummaryQueryService;

	@Operation(summary = "[SSE] 대기/진행/상태 통합 채널 "
		+ "(waiting-members-updated / in-progress-members-updated / session-status-updated)")
	@GetMapping(value = "/{sessionId}/events/room", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribeRoomEvents(
		Authentication authentication,
		@PathVariable Long sessionId
	) {
		Long memberId = null;
		if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User user) {
			memberId = user.getMemberId();
		}

		SseEmitter emitter = roomEventService.subscribeRoom(sessionId, memberId);

		try {
			SessionResponseDTO.SessionStartResponseDTO statusDto = sessionQueryService.getSessionStatus(sessionId);
			roomEventService.sendToRoomEmitter(emitter, RoomEventService.EVENT_SESSION_STATUS_UPDATED, statusDto);

			if (statusDto.getStatus() == SessionRoomStatus.IN_PROGRESS) {
				roomEventService.sendToRoomEmitter(
					emitter,
					RoomEventService.EVENT_IN_PROGRESS_UPDATED,
					sessionQueryService.getCurrentSessionRoom(sessionId)
				);
			} else {
				SessionResponseDTO.SessionWaitingRoomResponseDTO<SessionResponseDTO.WaitingResponseDTO> waitingDto =
					SessionResponseDTO.SessionWaitingRoomResponseDTO.<SessionResponseDTO.WaitingResponseDTO>builder()
						.eventType(EventType.ROOM_UPDATE)
						.data(sessionQueryService.getCurrentWaitingRoom(sessionId))
						.build();
				roomEventService.sendToRoomEmitter(emitter, RoomEventService.EVENT_WAITING_UPDATED, waitingDto);
			}
		} catch (Exception e) {
			emitter.completeWithError(e);
		}
		return emitter;
	}

	@Operation(summary = "[SSE] 리액션 통합 채널 (reaction-summary-updated, 본인이 받은 리액션 + 세션 전체 합계)")
	@GetMapping(value = "/{sessionId}/events/reactions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribeReactionEvents(
		Authentication authentication,
		@PathVariable Long sessionId
	) {
		Long memberId = null;
		if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User user) {
			memberId = user.getMemberId();
		}

		SseEmitter emitter = reactionEventService.subscribeReaction(sessionId, memberId);

		try {
			reactionEventService.sendToReactionEmitter(
				emitter,
				reactionSummaryQueryService.getReactionSummaryForMember(sessionId, memberId)
			);
		} catch (Exception e) {
			emitter.completeWithError(e);
		}

		return emitter;
	}
}