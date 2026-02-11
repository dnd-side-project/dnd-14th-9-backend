package com.example.gak.domain.member.dto;

import com.example.gak.domain.common.entity.enums.SessionCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

		@Pattern(
			regexp = "^[a-zA-Z0-9가-힣]+$",
			message = "닉네임은 한글, 영어, 숫자만 가능합니다."
		)
		@NotBlank
		@Size(min = 2, max = 10)
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
