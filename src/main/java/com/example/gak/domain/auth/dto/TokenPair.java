package com.example.gak.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(name = "토큰 재발급 응답")
@RequiredArgsConstructor
@Getter
public class TokenPair {

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	private final String accessToken;

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
	private final String refreshToken;
}
