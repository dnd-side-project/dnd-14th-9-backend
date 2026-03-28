package com.example.gak.global.security.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

	public static final String OAUTH2_REFRESH_TOKEN_KEY_PREFIX = "RT(oauth2):";
	public static final String OAUTH2_ACCESS_TOKEN_KEY_PREFIX = "AT(oauth2):";

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
		String clientRegistrationId,
		String principalName
	) {
		return null;
	}

	@Override
	public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
		String memberId = principal.getName();
		OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
		OAuth2AccessToken accessToken = authorizedClient.getAccessToken();

		if (refreshToken != null) {
			String refreshTokenKey = OAUTH2_REFRESH_TOKEN_KEY_PREFIX + memberId;
			Duration refreshTokenDuration = Duration.between(
				Instant.now(),
				refreshToken.getExpiresAt() != null
					? refreshToken.getExpiresAt()
					: Instant.now().plus(30, ChronoUnit.DAYS)
			);
			stringRedisTemplate.opsForValue().set(
				refreshTokenKey,
				refreshToken.getTokenValue(),
				refreshTokenDuration
			);
		}

		String accessTokenKey = OAUTH2_ACCESS_TOKEN_KEY_PREFIX + memberId;
		Duration accessTokenDuration = Duration.between(
			Instant.now(),
			accessToken.getExpiresAt()
		);
		stringRedisTemplate.opsForValue().set(
			accessTokenKey,
			accessToken.getTokenValue(),
			accessTokenDuration
		);
	}

	@Override
	public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
	}
}
