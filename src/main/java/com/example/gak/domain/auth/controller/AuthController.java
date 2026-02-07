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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<TokenPair>> refreshToken(
		@CookieValue(value = "refreshToken", required = false) String refreshToken
	) {
		TokenPair tokenPair = authService.refreshToken(refreshToken, Instant.now());

		return ResponseEntity.ok(ApiResponse.onSuccess(tokenPair));
	}

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
