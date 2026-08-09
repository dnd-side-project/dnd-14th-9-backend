package com.example.gak.domain.session.service;

import static com.example.gak.domain.session.converter.SessionConverter.*;
import static com.example.gak.domain.session.entity.enums.SessionRoomStatus.*;
import static com.example.gak.global.apiPayload.code.GeneralErrorCode.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gak.domain.chat.repository.ChatMessageRepository;
import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.domain.record.entity.Record;
import com.example.gak.domain.record.repository.RecordRepository;
import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.SessionRequestDTO;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.Reaction;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.session.entity.enums.SessionParticipantRole;
import com.example.gak.domain.session.entity.enums.SessionParticipantStatus;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.repository.ReactionRepository;
import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.domain.task.entity.SubTask;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.SubTaskRepository;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.objectstorage.OciObjectStorageManager;
import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.InProgressRoomUpdateEvent;
import com.example.gak.global.redis.event.MemberKickedEvent;
import com.example.gak.global.redis.event.MemberReactionUpdateEvent;
import com.example.gak.global.redis.event.ReactionUpdateEvent;
import com.example.gak.global.redis.event.SessionStatusUpdateEvent;
import com.example.gak.global.redis.event.WaitingRoomUpdateEvent;
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
	private final ReactionRepository reactionRepository;
	private final ChatMessageRepository chatMessageRepository;

	private final RedisPublisher redisPublisher;

	private final OciObjectStorageManager objectStorageManager;
	private final ImageFileValidator imageFileValidator;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final RecordRepository recordRepository;

	public SessionRoom createSession(
		SessionRequestDTO.CreateSessionRequestDTO request,
		MultipartFile image,
		Long memberId
	) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_MEMBER));

		if (sessionRoomRepository.existsByMemberAndStatusIn(member, List.of(WAITING, IN_PROGRESS))) {
			throw new GeneralException(GeneralErrorCode.SESSION_CREATE_LIMIT_EXCEEDED);
		}

		LocalDateTime minAllowedStartTime = LocalDateTime.now().plusMinutes(MIN_START_MINUTES);
		if (request.getStartTime().isBefore(minAllowedStartTime)) {
			throw new GeneralException(GeneralErrorCode.SESSION_START_TIME_TOO_SOON);
		}

		Record hostRecord = recordRepository.findByMember(member);
		validateRequiredRatesAgainstHost(
			hostRecord,
			request.getRequiredFocusRate(),
			request.getRequiredAchievementRate()
		);

		String imageUrl;
		if (image != null && !image.isEmpty()) {
			imageFileValidator.validate(image);

			String keyName = objectStorageManager.generateSessionThumbnailKeyName();
			imageUrl = objectStorageManager.uploadFile(keyName, image);
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
			WAITING,
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

		Record participantRecord = recordRepository.findByMember(member);
		validateParticipantMeetsRequirement(participantRecord, targetSessionRoom);

		increaseCountOrThrow(targetSessionRoom, memberId);

		List<SessionRoomMember> sessionRoomMembers
			= sessionRoomMemberRepository.findBySessionRoom(targetSessionRoom);

		SessionParticipantRole role = determineRole(member, targetSessionRoom, sessionRoomMembers);
		SessionRoomMember sessionRoomMember = saveSessionRoomMember(targetSessionRoom, member, role);
		SessionResponseDTO.taskResponseDTO taskResponseDTO = saveGoalTask(targetSessionRoom, member, request);

		publishSessionRoomUpdateEvent(
			sessionRoomMember.getSessionRoom().getStatus(),
			sessionId
		);

		return tojoinSessionResponseDTO(sessionRoomMember, member, targetSessionRoom, taskResponseDTO);
	}

	private SessionParticipantRole determineRole(
		Member member,
		SessionRoom sessionRoom,
		List<SessionRoomMember> members
	) {
		boolean isRoomOwner = member.getId().equals(sessionRoom.getMember().getId());

		if (sessionRoom.getStatus() == WAITING) {
			return isRoomOwner ? SessionParticipantRole.HOST : SessionParticipantRole.PARTICIPANT;
		}

		if (members.isEmpty()) {
			return SessionParticipantRole.HOST;
		}

		return SessionParticipantRole.PARTICIPANT;
	}

	public void leaveSessionOnDisconnect(Long sessionId, Long memberId) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId).orElse(null);
		if (sessionRoom == null || sessionRoom.getStatus() == COMPLETED) {
			return;
		}

		int deleted = sessionRoomMemberRepository.deleteByMemberIdAndSessionRoomId(memberId, sessionId);
		if (deleted == 0) {
			return;
		}

		taskRepository.findWithSessionRoomBySessionRoomIdAndMemberId(sessionId, memberId)
			.ifPresent(task -> {
				taskRepository.delete(task);
				taskRepository.flush();
			});

		sessionRoomRepository.decreaseCount(sessionId);
		hostPermissionTransfer(sessionId);
		publishSessionRoomUpdateEvent(sessionRoom.getStatus(), sessionId);
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

		hostPermissionTransfer(sessionId);

		publishSessionRoomUpdateEvent(
			task.getSessionRoom().getStatus(),
			sessionId
		);
	}

	private void hostPermissionTransfer(Long sessionId) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (sessionRoom.getStatus() != SessionRoomStatus.IN_PROGRESS) {
			return;
		}

		List<SessionRoomMember> members =
			sessionRoomMemberRepository.findBySessionRoomId(sessionId);

		if (members.isEmpty()) {
			return;
		}

		boolean hasHost = members.stream()
			.anyMatch(m -> m.getRole() == SessionParticipantRole.HOST);

		if (hasHost) {
			return;
		}

		SessionRoomMember oldestMember = members.stream()
			.min(Comparator.comparing(SessionRoomMember::getCreatedAt))
			.orElseThrow();

		oldestMember.changeParticipantRole(SessionParticipantRole.HOST);
		sessionRoomMemberRepository.save(oldestMember);
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

		if (sessionRoomMember.getStatus() == SessionParticipantStatus.REST) {
			int seconds = (int)Duration.between(
				sessionRoomMember.getLastFocusTime(),
				LocalDateTime.now()
			).getSeconds();

			sessionRoomMember.updateFocusSeconds(seconds);
		} else if (sessionRoomMember.getStatus() == SessionParticipantStatus.FOCUSED) {
			sessionRoomMember.setLastFocusTime(LocalDateTime.now());
		}

		publishSessionRoomUpdateEvent(
			sessionRoomMember.getSessionRoom().getStatus(),
			sessionId
		);

		return toToggleSessionMemberStatusResponseDTO(sessionRoomMember);
	}

	public void forceExitMembers(
		Long sessionId,
		Long memberId,
		SessionRequestDTO.ForceExitMemberRequestDTO request
	) {
		SessionRoom sessionRoom = sessionRoomRepository.findWithMemberById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (sessionRoom.getStatus() != WAITING) {
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

		publishSessionRoomKickedMemberEvent(
			sessionId,
			targetMemberIds
		);

		publishSessionRoomUpdateEvent(
			sessionRoom.getStatus(),
			sessionId
		);
	}

	public void startSession(Long sessionId) {
		int updated = sessionRoomRepository.transitionStatus(sessionId, WAITING, SessionRoomStatus.IN_PROGRESS);
		if (updated == 0) {
			return;
		}

		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		List<SessionRoomMember> sessionRoomMembers = sessionRoom.getSessionRoomMembers();
		for (SessionRoomMember member : sessionRoomMembers) {
			member.setLastFocusTime(LocalDateTime.now());
		}

		applicationEventPublisher.publishEvent(
			new SessionStatusUpdateEvent(sessionId)
		);
	}

	public void endSession(Long sessionId) {
		int updated = sessionRoomRepository.transitionStatus(
			sessionId, SessionRoomStatus.IN_PROGRESS, SessionRoomStatus.COMPLETED);
		if (updated == 0) {
			return;
		}

		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		List<SessionRoomMember> sessionRoomMembers = sessionRoom.getSessionRoomMembers();
		if (sessionRoomMembers.isEmpty()) {
			applicationEventPublisher.publishEvent(new SessionStatusUpdateEvent(sessionId));
			return;
		}

		Map<Long, Task> taskByMemberId = taskRepository.findAllWithSubTasksBySessionRoomId(sessionId)
			.stream()
			.collect(Collectors.toMap(t -> t.getMember().getId(), Function.identity()));

		List<Long> memberIds = sessionRoomMembers.stream()
			.map(m -> m.getMember().getId())
			.toList();

		Map<Long, Record> recordByMemberId = recordRepository.findByMemberIdIn(memberIds)
			.stream()
			.collect(Collectors.toMap(r -> r.getMember().getId(), Function.identity()));

		for (SessionRoomMember member : sessionRoomMembers) {
			Long memberId = member.getMember().getId();
			Task task = taskByMemberId.get(memberId);

			if (task == null) {
				continue;
			}

			processMemberStats(member, task, sessionRoom, recordByMemberId.get(memberId));
		}

		applicationEventPublisher.publishEvent(
			new SessionStatusUpdateEvent(sessionId)
		);
	}

	public void postSessionResult(
		Long memberId,
		Long sessionId,
		SessionRequestDTO.SessionResultRequestDTO request
	) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (sessionRoom.getStatus() != SessionRoomStatus.COMPLETED) {
			throw new GeneralException(GeneralErrorCode.SESSION_RESULT_BEFORE_END);
		}

		SessionRoomMember sessionRoomMember = sessionRoomMemberRepository.findByMemberIdAndSessionRoomId(memberId,
				sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		sessionRoomMember.setOverallSeconds(request.getOverallSeconds());
		sessionRoomMember.setTotalFocusSeconds(request.getTotalFocusSeconds());
		sessionRoomMember.updateFocusRate();

		Task task = taskRepository.findWithSessionRoomBySessionRoomIdAndMemberId(sessionId, memberId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.TASK_NOT_FOUND_IN_SESSION));
		List<SubTask> subTasks = subTaskRepository.findByTaskId(task.getId());

		sessionRoomMember.updateAchievementRate(subTasks);
		Record record = recordRepository.findByMemberId(memberId);
		sessionSaveToRecord(record, sessionRoomMember, subTasks);
	}

	private void sessionSaveToRecord(
		Record record, SessionRoomMember sessionRoomMember, List<SubTask> subTasks
	) {
		if (record == null)
			return;

		record.increaseParticipationTime(sessionRoomMember.getOverallSeconds());
		record.increaseFocusedTime(sessionRoomMember.getTotalFocusSeconds());
		record.increaseTotalTodoCount(subTasks.size());
		record.increaseCompletedTodoCount(
			(int)subTasks.stream()
				.filter(SubTask::isCompleted)
				.count()
		);
		record.increaseSessionCategoryCount(
			sessionRoomMember.getSessionRoom().getCategory());
	}

	public SessionResponseDTO.EmojiActionResponseDTO reaction(
		Long sessionId,
		Long memberId,
		SessionRequestDTO.EmojiActionRequest request
	) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (sessionRoom.getStatus() != SessionRoomStatus.COMPLETED) {
			throw new GeneralException(GeneralErrorCode.SESSION_RESULT_BEFORE_END);
		}

		SessionRoomMember actor = sessionRoomMemberRepository
			.findByMemberIdAndSessionRoomId(memberId, sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		if (actor.getMember().getId().equals(request.getTargetMemberId())) {
			throw new GeneralException(GeneralErrorCode.CANNOT_REACT_TO_SELF);
		}

		SessionRoomMember target = sessionRoomMemberRepository
			.findByMemberIdAndSessionRoomId(request.getTargetMemberId(), sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		Record record = recordRepository.findByMember(target.getMember());

		Optional<Reaction> optional = reactionRepository
			.findBySessionRoomIdAndMemberIdAndTargetMemberId(
				sessionId,
				memberId,
				request.getTargetMemberId()
			);

		if (optional.isPresent()) {
			Reaction existing = optional.get();

			if (existing.getEmojiType() == request.getEmojiType()) {
				reactionRepository.delete(existing);

				if (record != null) {
					record.decreaseEmojiTypesCount(
						Map.of(existing.getEmojiType(), 1)
					);
				}

				applicationEventPublisher.publishEvent(
					new MemberReactionUpdateEvent(sessionId, request.getTargetMemberId())
				);
				applicationEventPublisher.publishEvent(
					new ReactionUpdateEvent(sessionId)
				);

				return SessionConverter.emojiDeleted(request.getTargetMemberId());
			}

			EmojiType before = existing.getEmojiType();
			EmojiType after = request.getEmojiType();

			existing.changeEmojiType(after);

			if (record != null) {
				record.decreaseEmojiTypesCount(Map.of(before, 1));
				record.increaseEmojiTypesCount(Map.of(after, 1));
			}

			applicationEventPublisher.publishEvent(
				new MemberReactionUpdateEvent(sessionId, request.getTargetMemberId())
			);
			applicationEventPublisher.publishEvent(
				new ReactionUpdateEvent(sessionId)
			);

			return SessionConverter.emojiUpdated(
				request.getTargetMemberId(),
				request.getEmojiType()
			);
		}

		Reaction emojiAction = Reaction.create(
			request.getEmojiType(),
			actor.getMember(),
			target.getMember(),
			sessionRoom
		);

		reactionRepository.save(emojiAction);

		if (record != null) {
			record.increaseEmojiTypesCount(
				Map.of(request.getEmojiType(), 1)
			);
		}

		applicationEventPublisher.publishEvent(
			new MemberReactionUpdateEvent(sessionId, request.getTargetMemberId())
		);
		applicationEventPublisher.publishEvent(
			new ReactionUpdateEvent(sessionId)
		);

		return SessionConverter.emojiUpdated(
			request.getTargetMemberId(),
			request.getEmojiType()
		);
	}

	public void sessionDelete(Long sessionId, Long memberId) {

		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (!sessionRoom.getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.NOT_SESSION_HOST);
		}

		if (sessionRoom.getStatus() != WAITING) {
			throw new GeneralException(GeneralErrorCode.SESSION_DELETE_ONLY_WAITING);
		}

		if (hasOtherParticipants(sessionRoom, memberId)) {
			throw new GeneralException(GeneralErrorCode.SESSION_DELETE_HAS_WAITING_USERS);
		}

		List<Task> tasks = taskRepository.findBySessionRoomId(sessionId);
		taskRepository.deleteAll(tasks);
		taskRepository.flush();

		chatMessageRepository.deleteBySessionRoomId(sessionId);
		chatMessageRepository.flush();

		sessionRoomRepository.delete(sessionRoom);
	}

	public void sessionPatch(
		Long sessionId,
		Long memberId,
		SessionRequestDTO.PatchSessionRequestDTO request,
		MultipartFile image
	) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.NOT_FOUND_SESSION));

		if (!sessionRoom.getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.NOT_SESSION_HOST);
		}

		if (sessionRoom.getStatus() != WAITING) {
			throw new GeneralException(GeneralErrorCode.SESSION_PATCH_ONLY_WAITING);
		}

		if (hasOtherParticipants(sessionRoom, memberId)) {
			throw new GeneralException(GeneralErrorCode.SESSION_PATCH_HAS_WAITING_USERS);
		}

		if (request.getStartTime() != null) {
			LocalDateTime minAllowedStartTime = LocalDateTime.now().plusMinutes(MIN_START_MINUTES);

			if (request.getStartTime().isBefore(minAllowedStartTime)) {
				throw new GeneralException(GeneralErrorCode.SESSION_START_TIME_TOO_SOON);
			}
		}

		Record hostRecord = recordRepository.findByMemberId(memberId);
		validateRequiredRatesAgainstHost(
			hostRecord,
			request.getRequiredFocusRate(),
			request.getRequiredAchievementRate()
		);

		if (image != null && !image.isEmpty()) {
			imageFileValidator.validate(image);

			String keyName = objectStorageManager.generateSessionThumbnailKeyName();
			String imageUrl = objectStorageManager.uploadFile(keyName, image);

			if (sessionRoom.getThumbnailImageUrl() != null && !sessionRoom.getThumbnailImageUrl().isEmpty()) {
				objectStorageManager.deleteFile(sessionRoom.getThumbnailImageUrl());
			}

			sessionRoom.changeThumbnailImageUrl(imageUrl);
		} else if (Boolean.TRUE.equals(request.getDeleteImage())) {
			if (sessionRoom.getThumbnailImageUrl() != null && !sessionRoom.getThumbnailImageUrl().isEmpty()) {
				objectStorageManager.deleteFile(sessionRoom.getThumbnailImageUrl());
			}

			sessionRoom.changeThumbnailImageUrl(null);
		}

		sessionRoom.updateSession(request);
	}

	private void validateRequiredRatesAgainstHost(
		Record hostRecord,
		Integer requiredFocusRate,
		Integer requiredAchievementRate
	) {
		if (requiredFocusRate != null && requiredFocusRate > hostRecord.getFocusRate()) {
			throw new GeneralException(GeneralErrorCode.REQUIRED_FOCUS_RATE_EXCEEDS_HOST_RATE);
		}

		if (requiredAchievementRate != null && requiredAchievementRate > hostRecord.getTodoCompletionRate()) {
			throw new GeneralException(GeneralErrorCode.REQUIRED_ACHIEVEMENT_RATE_EXCEEDS_HOST_RATE);
		}
	}

	private void validateParticipantMeetsRequirement(Record participantRecord, SessionRoom sessionRoom) {
		if (participantRecord.getFocusRate() < sessionRoom.getRequiredFocusRate()) {
			throw new GeneralException(GeneralErrorCode.SESSION_JOIN_FOCUS_RATE_NOT_MET);
		}

		if (participantRecord.getTodoCompletionRate() < sessionRoom.getRequiredAchievementRate()) {
			throw new GeneralException(GeneralErrorCode.SESSION_JOIN_ACHIEVEMENT_RATE_NOT_MET);
		}
	}

	private boolean hasOtherParticipants(SessionRoom sessionRoom, Long memberId) {
		return sessionRoom.getSessionRoomMembers().stream()
			.anyMatch(m -> !m.getMember().getId().equals(memberId));
	}

	private void processMemberStats(SessionRoomMember member, Task task, SessionRoom sessionRoom, Record record) {
		if (member.getStatus() == SessionParticipantStatus.FOCUSED) {
			int seconds = (int)Duration.between(
				member.getLastFocusTime(),
				sessionRoom.getEndTime()
			).getSeconds();
			member.updateFocusSeconds(seconds);
		}

		LocalDateTime participationStart = member.getCreatedAt().isBefore(sessionRoom.getStartTime())
			? sessionRoom.getStartTime()
			: member.getCreatedAt();

		member.setOverallSeconds(
			(int)Duration.between(participationStart, sessionRoom.getEndTime()).getSeconds()
		);
		member.updateFocusRate();

		List<SubTask> subTasks = task.getSubTasks();
		member.updateAchievementRate(subTasks);
		sessionSaveToRecord(record, member, subTasks);
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

	private void increaseCountOrThrow(SessionRoom targetSessionRoom, Long memberId) {
		int updated = sessionRoomRepository.increaseCountIfJoinable(targetSessionRoom.getId(), memberId);
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
		} else if (status == WAITING) {
			applicationEventPublisher.publishEvent(
				new WaitingRoomUpdateEvent(sessionId)
			);
		}
	}

	private void publishSessionRoomKickedMemberEvent(Long sessionId, List<Long> memberIds) {
		applicationEventPublisher.publishEvent(
			new MemberKickedEvent(sessionId, memberIds)
		);
	}
}
