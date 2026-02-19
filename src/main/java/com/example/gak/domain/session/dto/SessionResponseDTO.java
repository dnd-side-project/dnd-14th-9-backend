package com.example.gak.domain.session.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionParticipantStatus;

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
}
