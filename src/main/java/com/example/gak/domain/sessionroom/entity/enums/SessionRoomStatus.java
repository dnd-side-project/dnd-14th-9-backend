package com.example.gak.domain.sessionroom.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionRoomStatus {

	WAITING("대기"),
	IN_PROGRESS("진행"),
	COMPLETED("완료");

	private final String displayName;
}
