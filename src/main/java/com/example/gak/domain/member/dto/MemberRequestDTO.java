package com.example.gak.domain.member.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

public class MemberRequestDTO {

	@Builder
	@Getter
	public static class UpdateMemberNicknameRequestDTO {

		@Min(2)
		@Max(12)
		private String nickname;
	}
}
