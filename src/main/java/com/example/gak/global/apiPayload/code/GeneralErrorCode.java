package com.example.gak.global.apiPayload.code;

import org.springframework.http.HttpStatus;

import com.example.gak.global.apiPayload.dto.ErrorReasonDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 처리 중 오류가 발생했습니다."),
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400_1", "잘못된 요청입니다."),

	// 인증 관련
	INVALID_AUTHORIZATION_HEADER(HttpStatus.UNAUTHORIZED, "AUTH401_1", "올바르지 않은 Authorization 헤더입니다."),
	INVALID_TOKEN_FORMAT(HttpStatus.UNAUTHORIZED, "AUTH401_2", "토큰 형식이 올바르지 않습니다."),
	ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401_3", "기한이 만료된 Access 토큰입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ErrorReasonDTO getReason() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(false)
			.build();
	}

	@Override
	public ErrorReasonDTO getReasonHttpStatus() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(false)
			.httpStatus(httpStatus)
			.build();
	}
}
