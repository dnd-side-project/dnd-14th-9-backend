package com.example.gak.domain.session.controller;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.session.dto.SessionRequestDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.enums.DurationRange;
import com.example.gak.domain.session.enums.SessionSort;
import com.example.gak.domain.session.enums.TimeSlot;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.service.SessionCommandService;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static com.example.gak.domain.session.converter.SessionConverter.toCreateSessionResponseDTO;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionQueryService sessionQueryService;
    private final SessionCommandService sessionCommandService;

    @Operation(summary = "세션 목록 조회 API")
    @GetMapping
    public ApiResponse<SessionResponseDTO.SessionCardResponseListDTO> getSessions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SessionCategory category,
            @RequestParam(required = false, defaultValue = "POPULAR") SessionSort sort,

            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,

            @RequestParam(required = false) List<TimeSlot> timeSlots,
            @RequestParam(required = false) DurationRange durationRange,

            @RequestParam(required = false) Integer participants,

            @RequestParam(required = false) Integer requiredFocusRate,
            @RequestParam(required = false) Integer requiredAchievementRate,

            @RequestParam(name = "page", defaultValue = "1") @Min(1)Integer page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1)Integer size
    ){
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
            @RequestPart("request") @Valid SessionRequestDTO.CreateSessionRequestDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ){
        Long memberId = oAuth2User.getMemberId();
        SessionRoom sessionRoom = sessionCommandService.createSession(request, image, memberId);
        return ApiResponse.onSuccess(toCreateSessionResponseDTO(sessionRoom));
    }

    @Operation(summary = "세션 상세 조회 API")
    @GetMapping("/{sessionId}")
    public ApiResponse<SessionResponseDTO.SessionDetailResponseDTO> getSessionDetail(
            @PathVariable Long sessionId
    ){
        return ApiResponse.onSuccess(sessionQueryService.getSessionDetail(sessionId));
    }

    @Operation(summary = "세션 참여 API")
    @PostMapping("/{sessionId}/join")
    public ApiResponse<SessionResponseDTO.joinSessionResponseDTO> joinSession(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestBody SessionRequestDTO.SessionJoinRequestDTO request,
            @PathVariable Long sessionId
    ){
        return ApiResponse.onSuccess(
                sessionCommandService.joinSession(oAuth2User.getMemberId(), sessionId, request)
        );
    }
}