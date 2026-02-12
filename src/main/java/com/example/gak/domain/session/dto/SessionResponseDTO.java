package com.example.gak.domain.session.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class SessionResponseDTO {

    @Builder
    @Getter
    public static class SessionCardResponseDTO{
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

    @Builder
    @Getter
    public static class CreateSessionResponseDTO {
        Long createdSessionId;
    }
}
