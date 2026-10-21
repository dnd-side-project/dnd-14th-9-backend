package com.example.gak.global.security.oauth2;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException {
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken)authentication;
		String registrationId = oauthToken.getAuthorizedClientRegistrationId();

		// callback의 state로 codeChallenge 조회
		String state = request.getParameter("state");

		if (state == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String codeChallenge = stringRedisTemplate.opsForValue()
			.get("oauth:challenge:" + state);

		if (codeChallenge == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "codeChallenge 저장 만료");
			return;
		}

		// code 생성 및 Redis 저장
		String code = UUID.randomUUID().toString();

		stringRedisTemplate.opsForValue().set(
			"oauth:challenge:" + code,
			codeChallenge,
			Duration.ofMinutes(1)
		);

		String origin = null;
		if (state != null && state.contains("|")) {
			origin = state.split("\\|")[0];
		}
		if (origin == null || origin.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "origin 정보 없음");
			return;
		}

		String redirectUrl = UriComponentsBuilder
			.fromUriString(origin + "/api/auth/callback/" + registrationId)
			.queryParam("code", code)
			.build()
			.toUriString();

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
		long memberId = Long.parseLong(oAuth2User.getName());

		stringRedisTemplate.opsForValue().set(
			"oauth:member:" + code,
			Long.toString(memberId),
			Duration.ofMinutes(1)
		);

		response.sendRedirect(redirectUrl);
	}
}
