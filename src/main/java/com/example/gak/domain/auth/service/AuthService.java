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
		Long memberId;
		try {
			memberId = jwtProvider.extractMemberId(refreshToken);
		} catch (ExpiredJwtException e) {
			throw new GeneralException(GeneralErrorCode.REFRESH_TOKEN_EXPIRED);
		} catch (JwtException e) {
			throw new GeneralException(GeneralErrorCode.INVALID_TOKEN_FORMAT);
		}

		if (jwtService.getRefreshToken(memberId) == null) {
			throw new GeneralException(GeneralErrorCode.NOT_FOUND_REFRESH_TOKEN);
		}
		jwtService.deleteRefreshToken(memberId);

		String accessToken = jwtProvider.createAccessToken(memberId, now);
		refreshToken = jwtProvider.createRefreshToken(memberId, now);
		jwtService.saveRefreshToken(memberId, refreshToken, now);

		return new TokenPair(accessToken, refreshToken);
	}
}
