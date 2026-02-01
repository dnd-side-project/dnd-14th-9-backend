package com.example.gak.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtProvider {

	private static final Duration ACCESS_TOKEN_EXPIRATION_TIME = Duration.ofHours(1);
	private static final Duration REFRESH_TOKEN_EXPIRATION_TIME = Duration.ofDays(30);

	private final SecretKey secretKey;

	public JwtProvider(@Value("${jwt.secret}") String secret) {
		this.secretKey = new SecretKeySpec(
			secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm()
		);
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

	public void validate(String token) {
		Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token);
	}

	public Long extractMemberId(String token) {
		return Long.valueOf(
			parseClaims(token).getSubject()
		);
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
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
