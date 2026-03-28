package com.example.gak.global.security.oauth2.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient kakaoRestClient() {
		return RestClient.builder()
			.baseUrl("https://kapi.kakao.com/v1/user")
			.build();
	}

	@Bean
	public RestClient googleRestClient() {
		return RestClient.builder()
			.baseUrl("https://oauth2.googleapis.com")
			.build();
	}
}
