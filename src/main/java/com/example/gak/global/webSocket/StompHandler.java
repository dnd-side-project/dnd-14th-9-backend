package com.example.gak.global.webSocket;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.gak.domain.session.service.SessionQueryService;
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

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor =
			MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor == null || accessor.getCommand() == null) {
			return message;
		}

		switch (accessor.getCommand()) {
			case CONNECT -> handleConnect(accessor);
			case SEND -> handleSend(accessor);
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

	private void handleSend(StompHeaderAccessor accessor) {
		String destination = accessor.getDestination();
		if (destination == null || !destination.startsWith("/pub/chat/")) {
			return;
		}

		if (accessor.getUser() == null) {
			throw new GeneralException(GeneralErrorCode.UNAUTHORIZED);
		}

		try {
			String[] parts = destination.split("/");
			Long sessionId = Long.parseLong(parts[3]);

			UsernamePasswordAuthenticationToken authToken =
				(UsernamePasswordAuthenticationToken)accessor.getUser();
			StompPrincipal principal = (StompPrincipal)authToken.getPrincipal();
			Long memberId = principal.getMemberId();

			boolean isHost = sessionQueryService.isHost(sessionId, memberId);
			if (!isHost) {
				throw new GeneralException(GeneralErrorCode.ONLY_HOST_CAN_CHAT);
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
