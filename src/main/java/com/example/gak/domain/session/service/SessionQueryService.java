package com.example.gak.domain.session.service;

import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.enums.DurationRange;
import com.example.gak.domain.session.enums.SessionSort;
import com.example.gak.domain.session.enums.TimeSlot;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.domain.session.repository.SessionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionQueryService {

    private final SessionRoomRepository sessionRoomRepository;

    public SessionResponseDTO.SessionCardResponseListDTO getSessions(
            String keyword,
            SessionCategory category,
            SessionSort sort,
            LocalDate startDate,
            LocalDate endDate,
            List<TimeSlot> timeSlots,
            DurationRange durationRange,
            Integer participants,
            Integer requiredFocusRate,
            Integer requiredAchievementRate,
            int page,
            int size
    ){
        Sort sortOption = createSortOption(sort.toString());
        PageRequest pageRequest = PageRequest.of(page-1, size, sortOption);

        Page<SessionRoom> sessionRooms = sessionRoomRepository.findAll(
                SessionSpecification.sessionRoomFilter(
                        keyword,
                        category,
                        startDate,
                        endDate,
                        timeSlots,
                        durationRange,
                        participants,
                        requiredFocusRate,
                        requiredAchievementRate
                ),
                pageRequest
        );

        List<SessionResponseDTO.SessionCardResponseDTO> searchResult =
                sessionRooms.getContent().stream()
                        .map(SessionConverter::toSessionCardResponseDTO)
                        .toList();

        SessionResponseDTO.SessionCardResponseListDTO result =
                SessionConverter.toSessionCardResponseListDTO(searchResult,sessionRooms);

        return result;
    }

    private Sort createSortOption(String sort) {
        return switch (sort) {
            case "POPULAR" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "LATEST" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "DEADLINE_APPROACHING" -> Sort.by(Sort.Direction.ASC, "startTime");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
