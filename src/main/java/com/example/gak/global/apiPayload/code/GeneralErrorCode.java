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
	UNSUPPORTED_PROVIDER(HttpStatus.UNAUTHORIZED, "AUTH401_4", "지원하지 않는 소셜 로그인 제공자입니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401_5", "기한이 만료된 Refresh 토큰입니다."),
	NOT_FOUND_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401_6", "존재하지 않는 Refresh 토큰입니다."),
	REFRESH_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH401_7", "Refresh 토큰이 전달되지 않았습니다."),
	REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH401_8", "Refresh 토큰 정보가 일치하지 않습니다."),

	// 회원 관련
	NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "MEMBER404_1", "존재하지 않는 회원입니다."),
	HAS_ACTIVE_SESSION(HttpStatus.CONFLICT, "MEMBER409_1", "완료되지 않은 세션이 존재하여 탈퇴할 수 없습니다."),

	// AWS S3 관련
	S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "AWS500_1", "S3 업로드에 실패했습니다."),
	S3_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "AWS500_2", "S3 파일 삭제에 실패했습니다."),
	S3_DOWNLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "AWS500_3", "S3 파일 조회에 실패했습니다."),

	// 세션 관련
	SESSION_START_TIME_TOO_SOON(HttpStatus.BAD_REQUEST, "SESSION400_1", "세션 시작 시간은 현재 시각 기준 5분 이후로 설정해야 합니다."),
	NOT_FOUND_SESSION(HttpStatus.NOT_FOUND, "SESSION404_1", "존재하지 않는 세션입니다."),

	// 파일 관련
	INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "FILE400_1", "허용되지 않은 이미지 형식입니다."),
	FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE400_2", "이미지 파일 크기가 너무 큽니다."),
	FILE_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "FILE400_3", "파일 검증에 실패했습니다.");


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
