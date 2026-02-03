package com.example.gak.global.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccessTokenFreeUrls {

	public static final String[] PATHS = {
		"/api/v1/auth/refresh",
		"/health",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};
}
