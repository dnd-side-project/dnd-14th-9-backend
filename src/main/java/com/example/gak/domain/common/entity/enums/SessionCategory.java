package com.example.gak.domain.common.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SessionCategory {

	DEVELOPMENT("개발"),
	DESIGN("디자인"),
	PLANNING_PM("기획 / PM"),
	CAREER_SELF_DEVELOPMENT("커리어 · 자기계발"),
	STUDY_READING("스터디 · 독서"),
	CREATIVE("크리에이티브"),
	TEAM_PROJECT("팀 프로젝트"),
	FREE("자유");

	private final String displayName;
}
