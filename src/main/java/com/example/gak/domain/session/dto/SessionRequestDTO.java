package com.example.gak.domain.session.dto;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class SessionRequestDTO {

    @Getter
    @Builder
    public static class CreateSessionRequestDTO{
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

    @Getter
    @Builder
    public static class SessionJoinRequestDTO{
        private String goal;
        private List<String> todos;
    }
}
