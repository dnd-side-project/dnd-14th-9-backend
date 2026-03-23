package com.example.gak.global.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccessTokenFreeUrls {

	public static final String[] PATHS = {
		"/api/v1/auth/refresh",
		"/health",
		"/swagger-ui/**",
		"/v3/api-docs/**",
		"/api/v1/sessions",
		"/api/v1/sessions/{sessionId:\\d+}/waiting-room/events",
		"/api/v1/sessions/{sessionId:\\d+}/in-progress/events",
		"/api/v1/sessions/{sessionId:\\d+}/status/events",
		"/api/v1/sessions/{sessionId:\\d+}/reaction/events",
		"/api/v1/sessions/{sessionId:\\d+}/members/{memberId:\\d+}/reaction/events",
		"/ws/**"
	};
}