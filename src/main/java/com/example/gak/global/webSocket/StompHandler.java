package com.example.gak.global.webSocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.gak.domain.chat.dto.ChatResponseDTO;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.apiPayload.code.BaseErrorCode;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.security.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer ";

	private final JwtProvider jwtProvider;
	private final SessionQueryService sessionQueryService;

	@Lazy
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor =
			MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		switch (accessor.getCommand()) {
			case CONNECT -> handleConnect(accessor);
			case SUBSCRIBE -> {
				return handleSubscribe(accessor, message);
			}
			case SEND -> {
				return handleSend(accessor, message);
			}
		}

		return message;
	}

	private void handleConnect(StompHeaderAccessor accessor) {
		String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith(BEARER)) {
			throw new GeneralException(GeneralErrorCode.INVALID_AUTHORIZATION_HEADER);
		}

		String token = authHeader.substring(BEARER.length());

		try {
			Long memberId = jwtProvider.extractMemberId(token);
			StompPrincipal principal = new StompPrincipal(memberId);
			accessor.setUser(
				new UsernamePasswordAuthenticationToken(principal, null, List.of())
			);
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			throw new GeneralException(GeneralErrorCode.ACCESS_TOKEN_EXPIRED);
		} catch (io.jsonwebtoken.JwtException e) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN_FORMAT);
		}
	}

	private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
		String destination = accessor.getDestination();
		if (destination == null || !destination.startsWith("/sub/chat/")) {
			return message;
		}

		if (accessor.getUser() == null) {
			return null;
		}

		String[] parts = destination.split("/");
		if (parts.length < 4) {
			return null;
		}

		Long sessionId;
		try {
			sessionId = Long.parseLong(parts[3]);
		} catch (NumberFormatException e) {
			return null;
		}

		UsernamePasswordAuthenticationToken authToken =
			(UsernamePasswordAuthenticationToken)accessor.getUser();
		StompPrincipal principal = (StompPrincipal)authToken.getPrincipal();
		String memberName = principal.getName();

		if (!sessionQueryService.isJoined(sessionId, principal.getMemberId())) {
			return sendErrorAndDrop(memberName, GeneralErrorCode.SESSION_NOT_JOINED);
		}

		return message;
	}

	private Message<?> handleSend(StompHeaderAccessor accessor, Message<?> message) {
		String destination = accessor.getDestination();
		if (destination == null || !destination.startsWith("/pub/chat/")) {
			return message;
		}

		if (accessor.getUser() == null) {
			return null;
		}

		UsernamePasswordAuthenticationToken authToken =
			(UsernamePasswordAuthenticationToken)accessor.getUser();
		StompPrincipal principal = (StompPrincipal)authToken.getPrincipal();
		String memberName = principal.getName();

		String[] parts = destination.split("/");
		if (parts.length < 4) {
			return null;
		}

		Long sessionId;
		try {
			sessionId = Long.parseLong(parts[3]);
		} catch (NumberFormatException e) {
			return null;
		}

		try {
			boolean isHost = sessionQueryService.isHost(sessionId, principal.getMemberId());
			if (!isHost) {
				return sendErrorAndDrop(memberName, GeneralErrorCode.ONLY_HOST_CAN_CHAT);
			}
		} catch (GeneralException e) {
			return sendErrorAndDrop(memberName, e.getCode());
		}

		return message;
	}

	private Message<?> sendErrorAndDrop(String memberName, BaseErrorCode errorCode) {
		ChatResponseDTO.ChatErrorResponseDTO error = ChatResponseDTO.ChatErrorResponseDTO.builder()
			.code(errorCode.getReasonHttpStatus().getCode())
			.message(errorCode.getReasonHttpStatus().getMessage())
			.build();

		messagingTemplate.convertAndSendToUser(memberName, "/queue/chat/error", error);
		return null;
	}
}
