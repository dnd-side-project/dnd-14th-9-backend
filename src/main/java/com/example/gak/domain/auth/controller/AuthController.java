package com.example.gak.domain.auth.controller;

import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gak.domain.auth.dto.TokenPair;
import com.example.gak.domain.auth.service.AuthService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.AuthCookieProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "토큰 재발급 API")
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenPair>> refreshToken(
		@CookieValue(value = "refreshToken", required = false) String refreshToken
	) {
		TokenPair tokenPair = authService.refreshToken(refreshToken, Instant.now());

		return ResponseEntity.ok(ApiResponse.onSuccess(tokenPair));
	}

	@Operation(summary = "로그아웃 API")
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
		@CookieValue(value = "refreshToken", required = false) String refreshToken
	) {
		authService.logout(refreshToken);

		return ResponseEntity.ok()
			.header(
				HttpHeaders.SET_COOKIE,
				AuthCookieProvider.deleteRefreshTokenCookie().toString()
			)
			.body(ApiResponse.onSuccess(null));
	}
}
