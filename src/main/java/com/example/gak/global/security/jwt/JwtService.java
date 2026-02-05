package com.example.gak.global.security.jwt;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private static final String REFRESH_TOKEN_PREFIX = "RefreshToken:";

	private final StringRedisTemplate stringRedisTemplate;
	private final JwtProvider jwtProvider;

	public void saveRefreshToken(
		Long memberId,
		String refreshToken,
		Instant now
	) {
		String key = REFRESH_TOKEN_PREFIX + memberId;
		Duration ttl = jwtProvider.getRemainExpiration(refreshToken, now);

		stringRedisTemplate.opsForValue().set(key, refreshToken, ttl);
	}

	public String getRefreshToken(Long memberId) {
		String key = REFRESH_TOKEN_PREFIX + memberId;
		return stringRedisTemplate.opsForValue().get(key);
	}

	public void deleteRefreshToken(Long memberId) {
		String key = REFRESH_TOKEN_PREFIX + memberId;
		stringRedisTemplate.delete(key);
	}
}
