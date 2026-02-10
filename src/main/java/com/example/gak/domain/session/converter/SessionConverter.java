package com.example.gak.domain.session.converter;

import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import org.springframework.data.domain.Page;

import java.util.List;

public class SessionConverter {

    public static SessionResponseDTO.SessionCardResponseDTO toSessionCardResponseDTO(SessionRoom sessionRoom) {
        return SessionResponseDTO.SessionCardResponseDTO.builder()
                .category(sessionRoom.getCategory().getDisplayName())
                .title(sessionRoom.getTitle())
                .hostNickname(sessionRoom.getMember().getNickname())
                .imageUrl(sessionRoom.getThumbnailImageUrl())
                .currentParticipants(0) // TODO: 실시간 통신 구현 이후 실제 카운트로 수정
                .maxParticipants(sessionRoom.getMaxCapacity())
                .startTime(sessionRoom.getStartTime())
                .status(sessionRoom.getStatus().getDisplayName())
                .build();
    }

    public static SessionResponseDTO.SessionCardResponseListDTO toSessionCardResponseListDTO(
            List<SessionResponseDTO.SessionCardResponseDTO> sessionCards,
            Page<SessionRoom> page) {
        return SessionResponseDTO.SessionCardResponseListDTO.builder()
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .listSize(page.getSize())
                .sessions(sessionCards)
                .build();
    }
}
