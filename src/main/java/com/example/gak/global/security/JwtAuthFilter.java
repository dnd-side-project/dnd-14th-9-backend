package com.example.gak.global.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.gak.global.apiPayload.code.BaseErrorCode;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.dto.ErrorReasonDTO;
import com.example.gak.global.security.jwt.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		Long memberId;
		try {
			String authorizationHeader = request.getHeader("Authorization");
			if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
				sendErrorResponse(response, GeneralErrorCode.INVALID_AUTHORIZATION_HEADER);
				return;
			}

			String accessToken = authorizationHeader.split(" ")[1];
			memberId = jwtProvider.extractMemberId(accessToken);
		} catch (ExpiredJwtException e) {
			sendErrorResponse(response, GeneralErrorCode.ACCESS_TOKEN_EXPIRED);
			return;
		} catch (JwtException e) { // ExpiredJwtException을 제외한 나머지 JwtException 처리
			sendErrorResponse(response, GeneralErrorCode.INVALID_TOKEN_FORMAT);
			return;
		}

		/*CustomOAuth2User oAuth2User = new CustomOAuth2User(memberId);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				oAuth2User,
				Optional.empty(),
				Collections.emptyList()
			)
		);*/

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return Arrays.stream(AccessTokenFreeUrls.PATHS)
			.anyMatch(pattern -> pathMatcher.match(pattern, request.getServletPath()));
	}

	private void sendErrorResponse(HttpServletResponse response, BaseErrorCode errorCode) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ErrorReasonDTO errorResponse = errorCode.getReasonHttpStatus();
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
