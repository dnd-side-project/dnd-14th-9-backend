package com.example.gak.domain.session.controller;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.session.enums.DurationRange;
import com.example.gak.domain.session.enums.SessionSort;
import com.example.gak.domain.session.enums.TimeSlot;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.service.SessionQueryService;
import com.example.gak.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionQueryService sessionQueryService;

    @Operation(summary = "세션 목록 조회 API")
    @GetMapping
    public ApiResponse<SessionResponseDTO.sessionCardResponseListDTO> getSessions(
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
}
