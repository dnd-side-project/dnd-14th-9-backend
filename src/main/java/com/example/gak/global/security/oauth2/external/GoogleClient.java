package com.example.gak.global.security.oauth2.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GoogleClient {

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String clientSecret;

	private final RestClient googleRestClient;

	public TokenResponse reissueToken(String refreshToken) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("grant_type", "refresh_token");
		formData.add("client_id", clientId);
		formData.add("client_secret", clientSecret);
		formData.add("refresh_token", refreshToken);

		return googleRestClient.post()
			.uri("/token")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(formData)
			.retrieve()
			.body(TokenResponse.class);
	}

	public void unlink(String accessToken) {
		googleRestClient.post()
			.uri(uriBuilder -> uriBuilder
				.path("/revoke")
				.queryParam("token", accessToken)
				.build())
			.retrieve()
			.toBodilessEntity();
	}

	@Getter
	@RequiredArgsConstructor
	public static class TokenResponse {
		private final String token_type;
		private final String access_token;
		private final int expires_in;
	}
}
