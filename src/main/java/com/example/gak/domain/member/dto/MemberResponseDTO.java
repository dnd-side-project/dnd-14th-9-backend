package com.example.gak.domain.member.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.common.entity.enums.SessionCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class MemberResponseDTO {

	@Schema(name = "회원 프로필 정보 응답")
	@Builder
	@Getter
	public static class GetProfileResponseDTO {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private Long id;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private String nickname;

		@Schema(nullable = true)
		private String profileImageUrl;

		@Schema(nullable = true)
		private String email;

		@Schema(nullable = true)
		private String bio;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private String socialProvider;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long totalParticipationTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long focusedTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int focusRate;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int totalTodoCount;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int completedTodoCount;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int todoCompletionRate;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int participationSessionCount;

		@Schema(nullable = true)
		private SessionCategory firstInterestCategory;

		@Schema(nullable = true)
		private SessionCategory secondInterestCategory;

		@Schema(nullable = true)
		private SessionCategory thirdInterestCategory;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private boolean firstLogin;
	}

	@Schema(name = "회원 상세 정보 응답")
	@Builder
	@Getter
	public static class GetMemberResponseDTO {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private Long id;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private String nickname;

		@Schema(nullable = true)
		private String profileImageUrl;

		@Schema(nullable = true)
		private String email;

		@Schema(nullable = true)
		private String bio;

		@Schema(nullable = true)
		private SessionCategory firstInterestCategory;

		@Schema(nullable = true)
		private SessionCategory secondInterestCategory;

		@Schema(nullable = true)
		private SessionCategory thirdInterestCategory;
	}

	@Schema(name = "회원 업데이트 정보 응답")
	@Builder
	@Getter
	public static class UpdateMemberResponseDTO {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private Long id;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private String nickname;

		@Schema(nullable = true)
		private String profileImageUrl;

		@Schema(nullable = true)
		private String email;

		@Schema(nullable = true)
		private String bio;

		@Schema(nullable = true)
		private SessionCategory firstInterestCategory;

		@Schema(nullable = true)
		private SessionCategory secondInterestCategory;

		@Schema(nullable = true)
		private SessionCategory thirdInterestCategory;
	}

	@Schema(name = "회원 리포트 통계 정보 응답")
	@Builder
	@Getter
	public static class GetReportStats {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long focusedTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long totalParticipationTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int todoCompletionRate;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int focusRate;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private List<SessionParticipationStat> sessionParticipationStats;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private List<ReceivedEmojiStat> receivedEmojis;
	}

	@Schema(name = "세션 참여 현황 응답")
	@Builder
	@Getter
	public static class SessionParticipationStat {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private SessionCategory categoryName;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int count;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int rate;
	}

	@Schema(name = "받은 이모지 현황 응답")
	@Builder
	@Getter
	public static class ReceivedEmojiStat {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private EmojiType emojiName;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int count;
	}

	@Schema(name = "회원 리포트 참여 세션 목록 응답")
	@Builder
	@Getter
	public static class GetReportSessionsResponseDTO {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int listSize;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int totalPage;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long totalElements;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private boolean isFirst;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private boolean isLast;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private List<GetReportSessionResponseDTO> sessions;
	}

	@Schema(name = "회원 리포트 참여 세션 응답")
	@Builder
	@Getter
	public static class GetReportSessionResponseDTO {

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private String title;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private SessionCategory category;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int currentCount;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int maxCapacity;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long durationTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private LocalDateTime startTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private long focusedTime;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int focusRate;

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		private int todoCompletionRate;
	}
}
