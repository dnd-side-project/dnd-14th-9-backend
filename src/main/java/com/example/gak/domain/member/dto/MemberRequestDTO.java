package com.example.gak.domain.member.dto;

import com.example.gak.domain.common.entity.enums.SessionCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberRequestDTO {

	@Schema(name = "회원 닉네임 수정 요청")
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

	@Schema(name = "회원 이메일 수정 요청")
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Getter
	public static class UpdateMemberEmailRequestDTO {

		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
		@Email
		private String email;
	}

	@Schema(name = "회원 관심카테고리 수정 요청")
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@Getter
	public static class UpdateMemberInterestCategoriesRequestDTO {

		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
		private SessionCategory firstInterestCategory;

		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
		private SessionCategory secondInterestCategory;

		@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
		private SessionCategory thirdInterestCategory;
	}
}
