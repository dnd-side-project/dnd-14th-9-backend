package com.example.gak.domain.chat.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import com.example.gak.domain.chat.dto.ChatRequestDTO;
import com.example.gak.domain.chat.service.ChatCommandService;
import com.example.gak.global.webSocket.StompPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

	private final ChatCommandService chatCommandService;

	@MessageMapping("/chat/{sessionId}")
	public void chat(
		@DestinationVariable Long sessionId,
		@Payload ChatRequestDTO.SendChatMessageRequestDTO message,
		Principal principal
	) {
		Long memberId = extractMemberId(principal);
		chatCommandService.sendMessage(sessionId, memberId, message);
	}

	private Long extractMemberId(Principal principal) {
		if (principal instanceof StompPrincipal stomp) {
			return stomp.getMemberId();
		}
		if (principal instanceof UsernamePasswordAuthenticationToken auth &&
			auth.getPrincipal() instanceof StompPrincipal inner) {
			return inner.getMemberId();
		}
		throw new IllegalStateException("Principal이 StompPrincipal이 아닙니다: " + principal.getClass());
	}
}