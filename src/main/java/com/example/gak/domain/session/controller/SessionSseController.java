package com.example.gak.domain.session.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.sse.SseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "세션 SSE")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionSseController {

	private final SseService sseService;
	private final SessionQueryService sessionQueryService;

	@Operation(summary = "[SSE] 대기방 참여자 목록 조회")
	@GetMapping(value = "/sse/waiting/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribeWaitingRoom(@PathVariable Long sessionId) {

		SseEmitter emitter = sseService.subscribeWaiting(sessionId);

		try {
			sseService.sendWaiting(sessionId,
				sessionQueryService.getCurrentWaitingRoom(sessionId));
		} catch (Exception e) {
			emitter.completeWithError(e);
		}

		return emitter;
	}
}
