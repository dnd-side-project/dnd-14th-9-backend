package com.example.gak.domain.session.controller;

import static com.example.gak.domain.session.converter.SessionConverter.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.session.dto.SessionRequestDTO;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.enums.DurationRange;
import com.example.gak.domain.session.enums.SessionSort;
import com.example.gak.domain.session.enums.TimeSlot;
import com.example.gak.domain.session.service.SessionCommandService;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Tag(name = "세션 API")
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

	private final SessionQueryService sessionQueryService;
	private final SessionCommandService sessionCommandService;

	@Operation(summary = "세션 목록 조회 API")
	@GetMapping
	public ApiResponse<SessionResponseDTO.SessionCardResponseListDTO> getSessions(
		@Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword,
		@Parameter(description = "카테고리") @RequestParam(required = false) SessionCategory category,
		@Parameter(description = "정렬 조건") @RequestParam(required = false, defaultValue = "POPULAR") SessionSort sort,

		@Parameter(description = "시작일") @RequestParam(required = false) LocalDate startDate,
		@Parameter(description = "종료일") @RequestParam(required = false) LocalDate endDate,

		@Parameter(description = "세션 진행 시간대") @RequestParam(required = false) List<TimeSlot> timeSlots,
		@Parameter(description = "세션 소요 시간") @RequestParam(required = false) DurationRange durationRange,

		@Parameter(description = "참여자 수") @RequestParam(required = false) Integer participants,

		@Parameter(description = "필요 집중도") @RequestParam(required = false) Integer requiredFocusRate,
		@Parameter(description = "필요 성취도") @RequestParam(required = false) Integer requiredAchievementRate,

		@Parameter(description = "페이지 번호") @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
		@Parameter(description = "페이지 크기") @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size
	) {
		return ApiResponse.onSuccess(
			sessionQueryService.getSessions(
				keyword,
				category,
				sort,
				startDate,
				endDate,
				timeSlots,
				durationRange,
				participants,
				requiredFocusRate,
				requiredAchievementRate,
				page,
				size
			)
		);
	}

	@Operation(summary = "세션 생성 API")
	@PostMapping(path = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<SessionResponseDTO.CreateSessionResponseDTO> createSession(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@Parameter(description = "세션 생성 요청(JSON)") @RequestPart("request") @Valid SessionRequestDTO.CreateSessionRequestDTO request,
		@Parameter(description = "세션 썸네일 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile image
	) {
		Long memberId = oAuth2User.getMemberId();
		SessionRoom sessionRoom = sessionCommandService.createSession(request, image, memberId);
		return ApiResponse.onSuccess(toCreateSessionResponseDTO(sessionRoom));
	}

	@Operation(summary = "세션 상세 조회 API")
	@GetMapping("/{sessionId}")
	public ApiResponse<SessionResponseDTO.SessionDetailResponseDTO> getSessionDetail(
		@PathVariable Long sessionId
	) {
		return ApiResponse.onSuccess(sessionQueryService.getSessionDetail(sessionId));
	}

	@Operation(summary = "세션 참여 API")
	@PostMapping("/{sessionId}/join")
	public ApiResponse<SessionResponseDTO.joinSessionResponseDTO> joinSession(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@RequestBody @Valid SessionRequestDTO.SessionJoinRequestDTO request,
		@PathVariable Long sessionId
	) {
		return ApiResponse.onSuccess(
			sessionCommandService.joinSession(oAuth2User.getMemberId(), sessionId, request)
		);
	}

	@Operation(summary = "세션 나가기 API")
	@DeleteMapping("/{sessionId}/leave")
	public ApiResponse<Void> leaveSession(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@PathVariable Long sessionId
	) {
		sessionCommandService.leaveSession(sessionId, oAuth2User.getMemberId());
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "[대기방] 세션 진입 초기 참여자 목록 조회 API")
	@GetMapping("/{sessionId}/waiting-room")
	public ApiResponse<SessionResponseDTO.WaitingResponseDTO> getWaitingRoomMembers(
		@PathVariable Long sessionId
	) {
		return ApiResponse.onSuccess(sessionQueryService.getCurrentWaitingRoom(sessionId));
	}

	@Operation(summary = "[진행중] 세션 진입 시 초기 참여자 목록 조회 API")
	@GetMapping("/{sessionId}/in-progress")
	public ApiResponse<SessionResponseDTO.InProgressResponseDTO> getInProgressSessionMembers(
		@PathVariable Long sessionId
	) {
		return ApiResponse.onSuccess(sessionQueryService.getCurrentSessionRoom(sessionId));
	}

	@Operation(summary = "세션 진행 중 참여자 상태 (집중/자리비움) 토글 API")
	@PatchMapping("/{sessionId}/me/status")
	public ApiResponse<SessionResponseDTO.ToggleSessionMemberStatusResponseDTO> toggleParticipantStatus(
		@PathVariable Long sessionId,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		return ApiResponse.onSuccess(
			sessionCommandService.toggleSessionRoomMemberStatus(sessionId, oAuth2User.getMemberId()));
	}

	@Operation(summary = "사용자 강제 퇴장 API")
	@DeleteMapping("/{sessionId}/members")
	public ApiResponse<Void> forceExitParticipant(
		@PathVariable Long sessionId,
		@RequestBody SessionRequestDTO.ForceExitMemberRequestDTO request,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		sessionCommandService.forceExitMembers(sessionId, oAuth2User.getMemberId(), request);
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "세션 종료 후 결과 전송 API")
	@PostMapping("/{sessionId}/results")
	public ApiResponse<Void> postSessionResult(
		@PathVariable Long sessionId,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@RequestBody @Valid SessionRequestDTO.SessionResultRequestDTO request
	) {
		sessionCommandService.postSessionResult(oAuth2User.getMemberId(), sessionId, request);
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "세션 종료 후 나의 리포트 조회 API")
	@GetMapping("/{sessionId}/me/report")
	public ApiResponse<SessionResponseDTO.SessionResultResponseDTO> getSessionFocusReport(
		@PathVariable Long sessionId,
		@AuthenticationPrincipal CustomOAuth2User oAuth2User
	) {
		return ApiResponse.onSuccess(sessionQueryService.getSessionReport(sessionId, oAuth2User.getMemberId()));
	}
}