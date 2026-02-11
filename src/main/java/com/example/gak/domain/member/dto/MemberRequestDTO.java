package com.example.gak.domain.member.dto;

import com.example.gak.domain.common.entity.enums.SessionCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberRequestDTO {

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Getter
	public static class UpdateMemberNicknameRequestDTO {

		@NotBlank
		@Size(min = 2, max = 12)
		private String nickname;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Getter
	public static class UpdateMemberInterestCategoriesRequestDTO {
		private SessionCategory firstInterestCategory;
		private SessionCategory secondInterestCategory;
		private SessionCategory thirdInterestCategory;
	}
}
