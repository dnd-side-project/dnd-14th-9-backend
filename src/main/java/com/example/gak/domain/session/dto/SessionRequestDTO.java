package com.example.gak.domain.session.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class SessionRequestDTO {

	@Schema(name = "세션 생성 요청")
	@Getter
	@Builder
	public static class CreateSessionRequestDTO {
		@NotBlank
		@Size(max = 20)
		private String title;

		@NotBlank
		@Size(max = 50)
		private String summary;

		@NotBlank
		@Size(max = 100)
		private String notice;

		@NotNull
		private SessionCategory category;

		@NotNull
		@Future
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
		private LocalDateTime startTime;

		@NotNull
		@Positive
		private Integer sessionDurationMinutes;

		@NotNull
		@Positive
		private Integer maxParticipants;

		@Min(0)
		private Integer requiredFocusRate = 0;

		@Min(0)
		private Integer requiredAchievementRate = 0;
	}

	@Schema(name = "세션 수정 요청")
	@Getter
	@Builder
	public static class PatchSessionRequestDTO {

		@Size(min = 1, max = 20)
		@Pattern(regexp = "^(?!\\s*$).+", message = "공백만 입력할 수 없습니다.")
		private String title;

		@Size(min = 1, max = 50)
		@Pattern(regexp = "^(?!\\s*$).+", message = "공백만 입력할 수 없습니다.")
		private String summary;

		@Size(min = 1, max = 100)
		@Pattern(regexp = "^(?!\\s*$).+", message = "공백만 입력할 수 없습니다.")
		private String notice;

		private SessionCategory category;

		@Future
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
		private LocalDateTime startTime;

		@Positive
		private Integer sessionDurationMinutes;

		@Positive
		private Integer maxParticipants;

		@Min(0)
		private Integer requiredFocusRate;

		@Min(0)
		private Integer requiredAchievementRate;
	}

	@Schema(name = "세션 참여 요청")
	@Getter
	@Builder
	public static class SessionJoinRequestDTO {

		@NotNull
		@Size(max = 50)
		private String goal;

		@NotNull
		private List<@Size(max = 50) String> todos;
	}

	@Schema(name = "사용자 강퇴 요청")
	@Getter
	@Builder
	public static class ForceExitMemberRequestDTO {
		private List<Long> memberIds;
	}

	@Schema(name = "세션 결과 서버 전송 요청")
	@Getter
	@Builder
	public static class SessionResultRequestDTO {

		@NotNull(message = "총 집중 시간은 필수입니다.")
		@Min(value = 0)
		private Integer totalFocusSeconds;

		@NotNull(message = "총 참여 시간은 필수입니다.")
		@Min(value = 0)
		private Integer overallSeconds;
	}

	@Schema(name = "이모지 액션 요청")
	@Getter
	@Builder
	public static class EmojiActionRequest {

		@NotNull
		Long targetMemberId;

		@NotNull
		EmojiType emojiType;
	}
}
