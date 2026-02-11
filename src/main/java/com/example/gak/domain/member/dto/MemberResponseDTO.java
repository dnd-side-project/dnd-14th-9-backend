package com.example.gak.domain.member.dto;

import com.example.gak.domain.common.entity.enums.SessionCategory;

import lombok.Builder;
import lombok.Getter;

public class MemberResponseDTO {

	@Builder
	@Getter
	public static class GetMemberResponseDTO {
		private Long id;
		private String nickname;
		private String profileImageUrl;
		private String bio;
		private SessionCategory firstInterestCategory;
		private SessionCategory secondInterestCategory;
		private SessionCategory thirdInterestCategory;
		private boolean firstLogin;
	}

	@Builder
	@Getter
	public static class UpdateMemberResponseDTO {
		private Long id;
		private String nickname;
		private String profileImageUrl;
		private String bio;
		private SessionCategory firstInterestCategory;
		private SessionCategory secondInterestCategory;
		private SessionCategory thirdInterestCategory;
	}
}
