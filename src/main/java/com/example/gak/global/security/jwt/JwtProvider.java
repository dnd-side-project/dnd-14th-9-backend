package com.example.gak.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

	private static final Duration ACCESS_TOKEN_EXPIRATION_TIME = Duration.ofHours(1);
	private static final Duration REFRESH_TOKEN_EXPIRATION_TIME = Duration.ofDays(30);

	private final SecretKey secretKey;
	private final JwtParser jwtParser;

	public JwtProvider(@Value("${jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(
			secret.getBytes(StandardCharsets.UTF_8)
		);
		this.jwtParser = Jwts.parser()
			.verifyWith(secretKey)
			.build();
	}

	public String createAccessToken(Long memberId, Instant now) {
		return createToken(
			memberId,
			now,
			ACCESS_TOKEN_EXPIRATION_TIME
		);
	}

	public String createRefreshToken(Long memberId, Instant now) {
		return createToken(
			memberId,
			now,
			REFRESH_TOKEN_EXPIRATION_TIME
		);
	}

	public Long extractMemberId(String token) {
		return Long.valueOf(
			parseClaims(token).getSubject()
		);
	}

	public Duration getRemainExpiration(String token, Instant now) {
		Date expiration = parseClaims(token).getExpiration();
		Instant expirationInstant = expiration.toInstant();

		return Duration.between(now, expirationInstant);
	}

	private String createToken(
		Long memberId,
		Instant now,
		Duration ttl
	) {
		return Jwts.builder()
			.subject(memberId.toString())
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plus(ttl)))
			.signWith(secretKey)
			.compact();
	}

	private Claims parseClaims(String token) {
		return jwtParser.parseSignedClaims(token)
			.getPayload();
	}
}
