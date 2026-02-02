package com.example.gak.global.security;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthCookieProvider {

	public static ResponseCookie createAccessTokenCookie(String accessToken) {
		return ResponseCookie.from("accessToken", accessToken)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(Duration.ofHours(1))
			.build();
	}

	public static ResponseCookie createRefreshTokenCookie(String refreshToken) {
		return ResponseCookie.from("refreshToken", refreshToken)
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(Duration.ofDays(30))
			.build();
	}

	public static ResponseCookie deleteRefreshTokenCookie() {
		return ResponseCookie.from("refreshToken", "")
			.path("/")
			.httpOnly(true)
			.secure(true)
			.sameSite("None")
			.maxAge(0)
			.build();
	}
}
