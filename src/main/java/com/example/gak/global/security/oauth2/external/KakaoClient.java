package com.example.gak.global.security.oauth2.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoClient {

	private final RestClient kakaoClient;

	@Value("${oauth2.kakao.admin-key}")
	private String adminKey;

	public void unlink(String providerId) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("target_id_type", "user_id");
		formData.add("target_id", providerId);

		kakaoClient.post()
			.uri("/unlink")
			.header("Authorization", "KakaoAK " + adminKey)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(formData)
			.retrieve()
			.toBodilessEntity();
	}
}
