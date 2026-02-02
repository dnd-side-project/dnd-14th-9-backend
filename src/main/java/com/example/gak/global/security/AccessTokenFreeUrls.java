package com.example.gak.global.security;

public final class AccessTokenFreeUrls {

	private AccessTokenFreeUrls() {
	}

	public static final String[] PATHS = {
		"/api/v1/auth/refresh",
		"/health"
	};
}
