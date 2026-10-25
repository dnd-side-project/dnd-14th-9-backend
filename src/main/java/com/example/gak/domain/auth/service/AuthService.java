package com.example.gak.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.gak.domain.auth.dto.TokenPair;
import com.example.gak.domain.auth.dto.TokenVerifier;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.security.jwt.JwtProvider;
import com.example.gak.global.security.jwt.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtProvider jwtProvider;
	private final JwtService jwtService;
	private final StringRedisTemplate stringRedisTemplate;

	public TokenPair issueToken(TokenVerifier tokenVerifier) {
		String codeChallenge = stringRedisTemplate.opsForValue()
			.get("oauth:challenge:" + tokenVerifier.code());

		String memberIdValue = stringRedisTemplate.opsForValue()
			.get("oauth:member:" + tokenVerifier.code());

		String calculatedChallenge = sha256Base64Url(tokenVerifier.codeVerifier());

		if (codeChallenge == null || memberIdValue == null) {
			throw new GeneralException(GeneralErrorCode.CODE_MEMBER_ID_EXPIRED);
		}

		if (!codeChallenge.equals(calculatedChallenge)) {
			throw new GeneralException(GeneralErrorCode.INVALID_CODE);
		}

		long memberId = Long.parseLong(memberIdValue);

		Instant now = Instant.now();
		String accessToken = jwtProvider.createAccessToken(memberId, now);
		String refreshToken = jwtProvider.createRefreshToken(memberId, now);
		jwtService.saveRefreshToken(memberId, refreshToken, now);

		return new TokenPair(accessToken, refreshToken);
	}

	public TokenPair refreshToken(String refreshToken, Instant now) {
		Long memberId = validateRefreshTokenAndGetMemberId(refreshToken);

		jwtService.deleteRefreshToken(memberId);

		String accessToken = jwtProvider.createAccessToken(memberId, now);
		String newRefreshToken = jwtProvider.createRefreshToken(memberId, now);
		jwtService.saveRefreshToken(memberId, newRefreshToken, now);

		return new TokenPair(accessToken, newRefreshToken);
	}

	public void logout(String refreshToken) {
		Long memberId = validateRefreshTokenAndGetMemberId(refreshToken);

		jwtService.deleteRefreshToken(memberId);
	}

	private String sha256Base64Url(String value) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256")
				.digest(value.getBytes(StandardCharsets.US_ASCII));

			return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private Long validateRefreshTokenAndGetMemberId(String refreshToken) {
		if (refreshToken == null) {
			throw new GeneralException(GeneralErrorCode.REFRESH_TOKEN_REQUIRED);
		}

		if (!jwtProvider.isRefreshToken(refreshToken)) {
			throw new GeneralException(GeneralErrorCode.INVALID_REFRESH_TOKEN_TYPE);
		}

		Long memberId;
		try {
			memberId = jwtProvider.extractMemberId(refreshToken);
		} catch (ExpiredJwtException e) {
			throw new GeneralException(GeneralErrorCode.REFRESH_TOKEN_EXPIRED);
		} catch (JwtException e) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN_FORMAT);
		}

		String storedRefreshToken = jwtService.getRefreshToken(memberId);
		if (storedRefreshToken == null) {
			throw new GeneralException(GeneralErrorCode.REFRESH_TOKEN_NOT_FOUND);
		}
		if (!storedRefreshToken.equals(refreshToken)) {
			throw new GeneralException(GeneralErrorCode.REFRESH_TOKEN_MISMATCH);
		}

		return memberId;
	}
}
