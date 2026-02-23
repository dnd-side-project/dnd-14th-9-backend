package com.example.gak.domain.session.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionParticipantStatus;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class SessionResponseDTO {

	@Schema(name = "세션 단건 응답")
	@Builder
	@Getter
	public static class SessionCardResponseDTO {
		private Long sessionId;
		private String category;
		private String title;
		private String hostNickname;
		private String status;
		private int currentParticipants;
		private int maxParticipants;
		private int sessionDurationMinutes;
		private LocalDateTime startTime;
		private String imageUrl;
	}

	@Schema(name = "세션 목록 조회 응답")
	@Builder
	@Getter
	public static class SessionCardResponseListDTO {
		private Integer listSize;
		private Integer totalPage;
		private Long totalElements;
		private Boolean isFirst;
		private Boolean isLast;
		private List<SessionCardResponseDTO> sessions;
	}

	@Schema(name = "세션 생성 응답")
	@Builder
	@Getter
	public static class CreateSessionResponseDTO {
		Long createdSessionId;
	}

	@Schema(name = "세션 상세 조회 응답")
	@Builder
	@Getter
	public static class SessionDetailResponseDTO {
		private Long sessionId;
		private String category;
		private String title;
		private String hostNickname;
		private String status;
		private int currentParticipants;
		private int maxParticipants;
		private int sessionDurationMinutes;
		private LocalDateTime startTime;
		private String imageUrl;
		private String summary;
		private String notice;
		private Integer requiredFocusRate;
		private Integer requiredAchievementRate;
	}

	@Schema(name = "세션 참여 응답")
	@Builder
	@Getter
	public static class joinSessionResponseDTO {
		private Long sessionId;
		private Long memberId;
		private taskResponseDTO task;
		private SessionParticipantRole role;
	}

	@Schema(name = "TODO 단건 응답")
	@Builder
	@Getter
	public static class todoResponseDTO {
		private Long subtaskId;
		private String content;
	}

	@Schema(name = "세션 목표,TODO 응답")
	@Builder
	@Getter
	public static class taskResponseDTO {
		private Long taskId;
		private String goal;
		private List<todoResponseDTO> todos;
	}

	@Schema(name = "세션 대기방 참여자 목록 단일 응답")
	@Builder
	@Getter
	public static class WaitingMemberResponseDTO {
		private String nickname;
		private Long memberId;
		private String profileImageUrl;
		private Integer focusRate;
		private Integer achievementRate;
		private SessionParticipantRole role;
		private taskResponseDTO task;
	}

	@Schema(name = "세션 대기방 참여자 목록 응답")
	@Builder
	@Getter
	public static class WaitingResponseDTO {
		private Integer participantCount;
		private List<WaitingMemberResponseDTO> members;
	}

	@Schema(name = "세션 진행 중 참여자 상태 토글 응답")
	@Builder
	@Getter
	public static class ToggleSessionMemberStatusResponseDTO {
		private SessionParticipantStatus currentStatus;
	}

	@Schema(name = "세션 진행 중 참여자 목록 응답")
	@Builder
	@Getter
	public static class InProgressResponseDTO {
		private Integer participantCount;
		private Integer averageAchievementRate;
		private List<InProgressMemberResponseDTO> members;
	}

	@Schema(name = "세션 시작-종료 알림 응답")
	@Builder
	@Getter
	public static class SessionStartResponseDTO {
		private SessionRoomStatus status;
	}

	@Schema(name = "세션 진행 중 참여자 목록 단일 응답")
	@Builder
	@Getter
	public static class InProgressMemberResponseDTO {
		private String nickname;
		private Long memberId;
		private String profileImageUrl;
		private SessionParticipantRole role;
		private Integer achievementRate;
		private SessionParticipantStatus status;

		private SessionTaskResponseDTO task;
	}

	@Schema(name = "세션 목표,TODO 응답 - 세션 진행중")
	@Builder
	@Getter
	public static class SessionTaskResponseDTO {
		private Long taskId;
		private String goal;
		private List<SessionTodoResponseDTO> todos;
	}

	@Schema(name = "TODO 단건 응답 - 세션 진행중")
	@Builder
	@Getter
	public static class SessionTodoResponseDTO {
		private Long subtaskId;
		private String content;
		private Boolean isCompleted;
	}

	@Schema(name = "세션 결과 응답")
	@Builder
	@Getter
	public static class SessionResultResponseDTO {
		private Long sessionId;
		private Integer currentParticipants;
		private SessionMemberResultResponseDTO sessionMemberResult;
	}

	@Schema(name = "사용자 세션 결과 단건 응답")
	@Builder
	@Getter
	public static class SessionMemberResultResponseDTO {
		private Long memberId;
		private String nickname;
		private String profileImageUrl;
		private SessionParticipantRole role;

		private Integer focusRate;
		private Integer totalFocusSeconds;
		private Integer overallFocusSeconds;

		private Integer achievementRate;
		private SessionTaskResponseDTO task;

		private EmojiResultResponseDTO emojiResult;
	}

	@Schema(name = "받은 이모지 갯수 응답")
	@Builder
	@Getter
	public static class EmojiResultResponseDTO {
		private Integer heartCount;
		private Integer starCount;
		private Integer thumbsUpCount;
		private Integer thumbsDownCount;
	}

	@Schema(name = "세션 전체 결과 응답")
	@Builder
	@Getter
	public static class EndSessionResponseDTO {
		private Integer averageTotalFocusSeconds;
		private Integer averageOverallSeconds;
		private Integer averageAchievementRate;
		private Integer averageFocusRate;

		private List<EndSessionMemberResultResponseDTO> members;
		private EmojiResultResponseDTO emojiResult;
	}

	@Schema(name = "세션 전체 결과 참여자별 응답")
	@Builder
	@Getter
	public static class EndSessionMemberResultResponseDTO {
		private Long memberId;
		private String nickname;
		private String profileImageUrl;
		private SessionParticipantRole role;
		private String goal;
		private Integer focusRate;
		private Integer achievementRate;
	}
}