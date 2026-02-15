package com.example.gak.domain.session.service;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.domain.session.dto.SessionRequestDTO;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.domain.subtask.entity.SubTask;
import com.example.gak.domain.subtask.repository.SubTaskRepository;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.aws.AmazonS3Manager;
import com.example.gak.global.validator.ImageFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static com.example.gak.domain.session.converter.SessionConverter.tojoinSessionResponseDTO;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionCommandService {

    private static final long MIN_START_MINUTES = 5;

    private final MemberRepository memberRepository;
    private final SessionRoomRepository sessionRoomRepository;
    private final SessionRoomMemberRepository sessionRoomMemberRepository;
    private final SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;

    private final AmazonS3Manager amazonS3Manager;
    private final ImageFileValidator imageFileValidator;

    public SessionRoom createSession(
            SessionRequestDTO.CreateSessionRequestDTO request,
            MultipartFile image,
            Long memberId
    ){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

        LocalDateTime minAllowedStartTime  = LocalDateTime.now().plusMinutes(MIN_START_MINUTES);
        if (request.getStartTime().isBefore(minAllowedStartTime )) {
            throw new GeneralException(GeneralErrorCode.SESSION_START_TIME_TOO_SOON);
        }

        String imageUrl;
        if(image != null && !image.isEmpty()) {
            imageFileValidator.validate(image);

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

    public SessionResponseDTO.joinSessionResponseDTO joinSession(
            Long memberId,
            Long sessionId,
            SessionRequestDTO.SessionJoinRequestDTO request
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

        SessionRoom targetSessionRoom = sessionRoomRepository.findWithMemberById(sessionId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

        increaseCountOrThrow(targetSessionRoom);
        SessionParticipantRole role = determineRole(member, targetSessionRoom);
        SessionRoomMember sessionRoomMember = saveSessionRoomMember(targetSessionRoom, member, role);
        saveGoalTask(targetSessionRoom, member, request);

        return tojoinSessionResponseDTO(sessionRoomMember, member, targetSessionRoom, request);
    }

    private void saveGoalTask(SessionRoom sessionRoom, Member member, SessionRequestDTO.SessionJoinRequestDTO request) {
        Task newTask = new Task(request.getGoal(), sessionRoom, member);
        taskRepository.save(newTask);

        request.getTodos().forEach(todo -> {
            SubTask subTask = new SubTask(todo, newTask);
            subTaskRepository.save(subTask);
        });
    }

    private void increaseCountOrThrow(SessionRoom targetSessionRoom) {
        int updated = sessionRoomRepository.increaseCountIfAvailable(targetSessionRoom.getId());
        if (updated == 0) {
            SessionRoom currentSessionRoom = sessionRoomRepository.findById(targetSessionRoom.getId())
                    .orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));
            if (currentSessionRoom.getStatus() == SessionRoomStatus.COMPLETED) {
                throw new GeneralException(GeneralErrorCode.SESSION_ALREADY_COMPLETED);
            } else {
                throw new GeneralException(GeneralErrorCode.SESSION_CAPACITY_EXCEEDED);
            }
        }
    }

    private SessionParticipantRole determineRole(Member member, SessionRoom sessionRoom) {
        if (member.getId().equals(sessionRoom.getMember().getId()) &&
                sessionRoom.getStatus() == SessionRoomStatus.WAITING) {
            return SessionParticipantRole.HOST;
        }
        return SessionParticipantRole.PARTICIPANT;
    }

    private SessionRoomMember saveSessionRoomMember(SessionRoom sessionRoom, Member member, SessionParticipantRole role) {
        SessionRoomMember newMember = new SessionRoomMember(role, sessionRoom, member);
        try {
            return sessionRoomMemberRepository.save(newMember);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("uk_session_room_member_member_session")) {
                throw new GeneralException(GeneralErrorCode.SESSION_ALREADY_JOINED);
            }else {
                throw e;
            }
        }
    }
}