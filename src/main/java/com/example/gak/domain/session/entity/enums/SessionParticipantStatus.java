package com.example.gak.domain.session.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionParticipantStatus {

	FOCUSED("집중"),
	REST("자리 비움");

	private final String displayName;
}