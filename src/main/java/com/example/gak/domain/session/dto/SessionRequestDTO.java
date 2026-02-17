package com.example.gak.domain.session.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
}
