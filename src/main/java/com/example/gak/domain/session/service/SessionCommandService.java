package com.example.gak.domain.session.service;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.domain.session.dto.SessionRequestDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.aws.AmazonS3Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionCommandService {

    private static final long MIN_START_MINUTES = 5;

    private final MemberRepository memberRepository;
    private final SessionRoomRepository sessionRoomRepository;
    private final AmazonS3Manager amazonS3Manager;

    public SessionRoom createSession(
            SessionRequestDTO.CreateSessionRequestDTO request,
            MultipartFile image,
            Long memberId
    ){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.MEMBER_NOT_FOUND));

        LocalDateTime minAllowedStartTime  = LocalDateTime.now().plusMinutes(MIN_START_MINUTES);
        if (request.getStartTime().isBefore(minAllowedStartTime )) {
            throw new GeneralException(GeneralErrorCode.SESSION_START_TIME_TOO_SOON);
        }

        String imageUrl;
        if(image != null && !image.isEmpty()) {
            String keyName = amazonS3Manager.generateSessionThumbnailKeyName();
            imageUrl = amazonS3Manager.uploadFile(keyName, image);
        }else{
            imageUrl = ""; // 기본 이미지 디자인 완성 시 URL 추가
        }

        SessionRoom newSessionRoom =  new SessionRoom(
                request.getCategory(),
                request.getTitle(),
                request.getSummary(),
                request.getNotice(),
                imageUrl,
                request.getStartTime(),
                request.getSessionDurationMinutes(),
                request.getMaxParticipants(),
                SessionRoomStatus.WAITING,
                request.getRequiredFocusRate(),
                request.getRequiredAchievementRate(),
                member
        );
        sessionRoomRepository.save(newSessionRoom);
        return newSessionRoom;
    }
}