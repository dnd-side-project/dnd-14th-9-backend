package com.example.gak.domain.auth.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.example.gak.domain.auth.dto.TokenPair;
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

	private Long validateRefreshTokenAndGetMemberId(String refreshToken) {
		if (refreshToken == null) {
			throw new GeneralException(GeneralErrorCode.REFRESH_TOKEN_REQUIRED);
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
