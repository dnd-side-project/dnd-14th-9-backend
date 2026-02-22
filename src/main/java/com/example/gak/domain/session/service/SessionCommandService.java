package com.example.gak.domain.session.service;

import static com.example.gak.domain.session.converter.SessionConverter.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.example.gak.domain.task.entity.SubTask;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.SubTaskRepository;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.aws.AmazonS3Manager;
import com.example.gak.global.redis.InProgressRoomUpdateEvent;
import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.WaitingRoomUpdateEvent;
import com.example.gak.global.validator.ImageFileValidator;

import lombok.RequiredArgsConstructor;

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

	private final RedisPublisher redisPublisher;

	private final AmazonS3Manager amazonS3Manager;
	private final ImageFileValidator imageFileValidator;
	private final ApplicationEventPublisher applicationEventPublisher;

	public SessionRoom createSession(
		SessionRequestDTO.CreateSessionRequestDTO request,
		MultipartFile image,
		Long memberId
	) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

		LocalDateTime minAllowedStartTime = LocalDateTime.now().plusMinutes(MIN_START_MINUTES);
		if (request.getStartTime().isBefore(minAllowedStartTime)) {
			throw new GeneralException(GeneralErrorCode.SESSION_START_TIME_TOO_SOON);
		}

		String imageUrl;
		if (image != null && !image.isEmpty()) {
			imageFileValidator.validate(image);

			String keyName = amazonS3Manager.generateSessionThumbnailKeyName();
			imageUrl = amazonS3Manager.uploadFile(keyName, image);
		} else {
			imageUrl = ""; // 기본 이미지 디자인 완성 시 URL 추가
		}

		SessionRoom newSessionRoom = new SessionRoom(
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
		SessionResponseDTO.taskResponseDTO taskResponseDTO = saveGoalTask(targetSessionRoom, member, request);

		publishSessionRoomUpdateEvent(
			sessionRoomMember.getSessionRoom().getStatus(),
			sessionId
		);

		return tojoinSessionResponseDTO(sessionRoomMember, member, targetSessionRoom, taskResponseDTO);
	}

	public void leaveSession(Long sessionId, Long memberId) {
		int deleted = sessionRoomMemberRepository
			.deleteByMemberIdAndSessionRoomId(memberId, sessionId);
		if (deleted == 0) {
			throw new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED);
		}

		Task task = taskRepository.findWithSessionRoomBySessionRoomIdAndMemberId(sessionId, memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.TASK_NOT_FOUND_IN_SESSION));

		taskRepository.delete(task);
		taskRepository.flush();

		int updated = sessionRoomRepository.decreaseCount(sessionId);
		if (updated == 0) {
			throw new GeneralException(GeneralErrorCode.SESSION_INVALID_STATE);
		}

		publishSessionRoomUpdateEvent(
			task.getSessionRoom().getStatus(),
			sessionId
		);
	}

	public SessionResponseDTO.ToggleSessionMemberStatusResponseDTO toggleSessionRoomMemberStatus(Long sessionId,
		Long memberId) {
		SessionRoomMember sessionRoomMember = sessionRoomMemberRepository.findByMemberIdAndSessionRoomId(memberId,
				sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		if (sessionRoomMember.getSessionRoom().getStatus() != SessionRoomStatus.IN_PROGRESS) {
			throw new GeneralException(GeneralErrorCode.SESSION_INVALID_STATE);
		}

		sessionRoomMember.toggleStatus();

		redisPublisher.inProgressRoomPublish(sessionId);

		return toToggleSessionMemberStatusResponseDTO(sessionRoomMember);
	}

	public void forceExitMembers(
		Long sessionId,
		Long memberId,
		SessionRequestDTO.ForceExitMemberRequestDTO request
	) {
		SessionRoom sessionRoom = sessionRoomRepository.findWithMemberById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (sessionRoom.getStatus() != SessionRoomStatus.WAITING) {
			throw new GeneralException(GeneralErrorCode.SESSION_KICK_ALLOWED_ONLY_IN_WAITING);
		}

		SessionRoomMember host = sessionRoomMemberRepository
			.findByMemberIdAndSessionRoomId(memberId, sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		if (host.getRole() != SessionParticipantRole.HOST) {
			throw new GeneralException(GeneralErrorCode.SESSION_KICK_HOST_ONLY);
		}

		List<Long> targetMemberIds = request.getMemberIds();

		if (targetMemberIds.contains(memberId)) {
			throw new GeneralException(GeneralErrorCode.SESSION_KICK_SELF_NOT_ALLOWED);
		}

		int deletedCount = sessionRoomMemberRepository.deleteByMemberIdInAndSessionRoomId(targetMemberIds, sessionId);
		if (deletedCount != targetMemberIds.size()) {
			throw new GeneralException(GeneralErrorCode.SESSION_MEMBER_NOT_FOUND);
		}

		List<Task> tasks = taskRepository.findAllBySessionRoomIdAndMemberIdIn(sessionId, targetMemberIds);
		for (Task task : tasks) {
			taskRepository.delete(task);
			taskRepository.flush();
		}

		int updated = sessionRoomRepository.decreaseCountBy(sessionId, targetMemberIds.size());
		if (updated == 0) {
			throw new GeneralException(GeneralErrorCode.SESSION_INVALID_STATE);
		}

		publishSessionRoomUpdateEvent(
			sessionRoom.getStatus(),
			sessionId
		);
	}

	private SessionResponseDTO.taskResponseDTO saveGoalTask(SessionRoom sessionRoom, Member member,
		SessionRequestDTO.SessionJoinRequestDTO request) {
		Task newTask = new Task(request.getGoal(), sessionRoom, member);
		taskRepository.save(newTask);

		List<SubTask> subTasks = request.getTodos().stream()
			.map(todo -> new SubTask(todo, newTask))
			.toList();

		subTaskRepository.saveAll(subTasks);
		subTaskRepository.flush();

		List<SessionResponseDTO.todoResponseDTO> list = new ArrayList<>();
		for (SubTask s : subTasks) {
			list.add(toTodoResponseDTO(s));
		}

		return toTaskResponseDTO(list, newTask);
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

	private SessionRoomMember saveSessionRoomMember(SessionRoom sessionRoom, Member member,
		SessionParticipantRole role) {
		SessionRoomMember newMember = new SessionRoomMember(role, sessionRoom, member);
		try {
			return sessionRoomMemberRepository.save(newMember);
		} catch (DataIntegrityViolationException e) {
			throw new GeneralException(GeneralErrorCode.SESSION_ALREADY_JOINED);
		}
	}

	private void publishSessionRoomUpdateEvent(SessionRoomStatus status, Long sessionId) {
		if (status == SessionRoomStatus.IN_PROGRESS) {
			applicationEventPublisher.publishEvent(
				new InProgressRoomUpdateEvent(sessionId)
			);
		} else if (status == SessionRoomStatus.WAITING) {
			applicationEventPublisher.publishEvent(
				new WaitingRoomUpdateEvent(sessionId)
			);
		}
	}
}