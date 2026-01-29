package com.example.gak.domain.sessionroommember.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionParticipantRole {

	HOST("방장"),
	PARTICIPANT("참여자");

	private final String displayName;
}
