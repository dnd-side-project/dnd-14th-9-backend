package com.example.gak.domain.session.service;

import static com.example.gak.domain.session.converter.SessionConverter.*;
import static com.example.gak.global.apiPayload.code.GeneralErrorCode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.common.entity.enums.EmojiType;
import com.example.gak.domain.common.entity.enums.SessionCategory;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.record.entity.Record;
import com.example.gak.domain.record.repository.RecordRepository;
import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.session.enums.DurationRange;
import com.example.gak.domain.session.enums.SessionSort;
import com.example.gak.domain.session.enums.TimeSlot;
import com.example.gak.domain.session.repository.EmojiActionRepository;
import com.example.gak.domain.session.repository.SessionRoomMemberRepository;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.domain.session.repository.SessionSpecification;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionQueryService {

	private final SessionRoomRepository sessionRoomRepository;
	private final SessionRoomMemberRepository sessionRoomMemberRepository;
	private final RecordRepository recordRepository;
	private final TaskRepository taskRepository;
	private final EmojiActionRepository emojiActionRepository;

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
	) {
		Sort sortOption = createSortOption(sort);
		PageRequest pageRequest = PageRequest.of(page - 1, size, sortOption);

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
			SessionConverter.toSessionCardResponseListDTO(searchResult, sessionRooms);

		return result;
	}

	public SessionResponseDTO.SessionDetailResponseDTO getSessionDetail(Long sessionId) {
		SessionRoom sessionRoom = sessionRoomRepository.findWithMemberById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));
		return toSessionDetailResponseDTO(sessionRoom);
	}

	private Sort createSortOption(SessionSort sort) {
		return switch (sort) {
			case POPULAR -> Sort.by(Sort.Direction.DESC, "viewCount");
			case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
			case DEADLINE_APPROACHING -> Sort.by(Sort.Direction.ASC, "startTime");
			default -> Sort.by(Sort.Direction.DESC, "createdAt");
		};
	}

	public SessionResponseDTO.WaitingResponseDTO getCurrentWaitingRoom(Long sessionId) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		List<SessionResponseDTO.WaitingMemberResponseDTO> members =
			sessionRoomMemberRepository.findBySessionRoom(sessionRoom)
				.stream()
				.map(srm -> toWaitingMember(sessionId, srm))
				.toList();

		return SessionConverter.toWaitingResponseDTO(
			members.size(),
			members
		);
	}

	public List<Long> findSessionsToStart() {
		LocalDateTime now = LocalDateTime.now();

		List<SessionRoom> targetSessions = sessionRoomRepository.findByStatusAndStartTimeBefore(
			SessionRoomStatus.WAITING, now
		);

		return targetSessions.stream()
			.map(SessionRoom::getId)
			.toList();
	}

	public List<Long> findSessionsToEnd() {
		LocalDateTime now = LocalDateTime.now();

		List<SessionRoom> targetSessions = sessionRoomRepository.findByStatusAndEndTimeBefore(
			SessionRoomStatus.IN_PROGRESS, now
		);

		return targetSessions.stream()
			.map(SessionRoom::getId)
			.toList();
	}

	private SessionResponseDTO.WaitingMemberResponseDTO toWaitingMember(
		Long sessionId,
		SessionRoomMember srm
	) {
		Member member = srm.getMember();

		Record record = recordRepository.findByMember(member);

		Task task = taskRepository
			.findBySessionRoomIdAndMemberId(sessionId, member.getId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		List<SessionResponseDTO.todoResponseDTO> todos =
			task.getSubTasks().stream()
				.map(SessionConverter::toTodoResponseDTO)
				.toList();

		SessionResponseDTO.taskResponseDTO taskDTO =
			SessionConverter.toTaskResponseDTO(todos, task);

		return SessionConverter.toWaitingMemberResponseDTO(
			member,
			srm,
			taskDTO,
			record
		);
	}

	public SessionResponseDTO.InProgressResponseDTO getCurrentSessionRoom(Long sessionId) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		List<SessionResponseDTO.InProgressMemberResponseDTO> members =
			sessionRoomMemberRepository.findBySessionRoom(sessionRoom)
				.stream()
				.map(srm -> toSessionMember(sessionId, srm))
				.toList();

		int averageAchievementRate = (int)members.stream()
			.mapToInt(SessionResponseDTO.InProgressMemberResponseDTO::getAchievementRate)
			.average()
			.orElse(0.0);

		return SessionConverter.toInProgressResponseDTO(
			members.size(),
			averageAchievementRate,
			members
		);
	}

	public SessionResponseDTO.SessionStartResponseDTO getSessionStatus(Long sessionId) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		return toSessionStartResponseDTO(sessionRoom);
	}

	public SessionResponseDTO.EmojiResultResponseDTO getReactionStatus(Long sessionId) {
		sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		int sumHeartCount = emojiActionRepository
			.countBySessionRoomIdAndEmojiType(sessionId, EmojiType.HEART);

		int sumStarCount = emojiActionRepository
			.countBySessionRoomIdAndEmojiType(sessionId, EmojiType.STAR);

		int sumThumbsUpCount = emojiActionRepository
			.countBySessionRoomIdAndEmojiType(sessionId, EmojiType.THUMBS_UP);

		int sumThumbsDownCount = emojiActionRepository
			.countBySessionRoomIdAndEmojiType(sessionId, EmojiType.THUMBS_DOWN);

		return SessionConverter.toSumEmojiResultResponseDTO(
			sumHeartCount,
			sumStarCount,
			sumThumbsUpCount,
			sumThumbsDownCount
		);
	}

	public SessionResponseDTO.SessionResultResponseDTO getMemberSessionReport(
		Long sessionId,
		Long memberId
	) {
		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		SessionRoomMember srm = sessionRoomMemberRepository
			.findByMemberIdAndSessionRoomId(memberId, sessionId)
			.orElseThrow(() -> new GeneralException(SESSION_NOT_JOINED));

		Task task = taskRepository.findBySessionRoomIdAndMemberId(sessionId, memberId)
			.orElseThrow(() -> new GeneralException(TASK_NOT_FOUND_IN_SESSION));

		SessionResponseDTO.EmojiResultResponseDTO emojiResult =
			SessionConverter.toEmojiResultResponseDTO(srm);

		List<SessionResponseDTO.SessionTodoResponseDTO> todos =
			task.getSubTasks().stream()
				.map(SessionConverter::toSessionTodoResponseDTO)
				.toList();

		SessionResponseDTO.SessionTaskResponseDTO sessionTask =
			SessionConverter.toSessionTaskResponseDTO(task, todos);

		SessionResponseDTO.SessionMemberResultResponseDTO memberResult =
			SessionConverter.toSessionMemberResultResponseDTO(
				srm,
				emojiResult,
				sessionTask
			);

		return SessionConverter.toSessionResultResponseDTO(
			0,
			sessionRoom,
			memberResult
		);
	}

	public SessionResponseDTO.EndSessionResponseDTO getSessionReport(Long sessionId) {

		SessionRoom sessionRoom = sessionRoomRepository.findById(sessionId)
			.orElseThrow(() -> new GeneralException(NOT_FOUND_SESSION));

		List<SessionRoomMember> sessionRoomMembers =
			sessionRoomMemberRepository.findBySessionRoom(sessionRoom);

		if (sessionRoomMembers.isEmpty()) {
			return SessionConverter.toEndSessionResponseDTO(
				0, 0, 0, 0, List.of(),
				SessionConverter.toSumEmojiResultResponseDTO(0, 0, 0, 0)
			);
		}

		int avgTotalFocusSeconds = (int)sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getTotalFocusSeconds)
			.average()
			.orElse(0);

		int avgOverallSeconds = (int)sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getOverallSeconds)
			.average()
			.orElse(0);

		int avgAchievementRate = (int)sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getAchievementRate)
			.average()
			.orElse(0);

		int avgFocusRate = (int)sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getFocusRate)
			.average()
			.orElse(0);

		List<Task> tasks = taskRepository.findBySessionRoomId(sessionId);

		Map<Long, Task> taskMap = tasks.stream()
			.collect(Collectors.toMap(
				task -> task.getMember().getId(),
				Function.identity()
			));

		List<SessionResponseDTO.EndSessionMemberResultResponseDTO> members =
			sessionRoomMembers.stream()
				.map(srm -> {
					Task task = taskMap.get(srm.getMember().getId());
					if (task == null) {
						throw new GeneralException(TASK_NOT_FOUND_IN_SESSION);
					}
					return SessionConverter.toEndSessionMemberResultResponseDTO(
						srm,
						task.getGoal()
					);
				})
				.toList();

		int sumHeartCount = sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getHeartCount)
			.sum();

		int sumStarCount = sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getStarCount)
			.sum();

		int sumThumbsUpCount = sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getThumbsUpCount)
			.sum();

		int sumThumbsDownCount = sessionRoomMembers.stream()
			.mapToInt(SessionRoomMember::getThumbsDownCount)
			.sum();

		SessionResponseDTO.EmojiResultResponseDTO emojiResult =
			SessionConverter.toSumEmojiResultResponseDTO(
				sumHeartCount,
				sumStarCount,
				sumThumbsUpCount,
				sumThumbsDownCount
			);

		return SessionConverter.toEndSessionResponseDTO(
			avgTotalFocusSeconds,
			avgOverallSeconds,
			avgAchievementRate,
			avgFocusRate,
			members,
			emojiResult
		);
	}

	private SessionResponseDTO.InProgressMemberResponseDTO toSessionMember(
		Long sessionId,
		SessionRoomMember srm
	) {
		Member member = srm.getMember();

		Task task = taskRepository
			.findBySessionRoomIdAndMemberId(sessionId, member.getId())
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SESSION_NOT_JOINED));

		List<SessionResponseDTO.SessionTodoResponseDTO> todos =
			task.getSubTasks().stream()
				.map(SessionConverter::toSessionTodoResponseDTO)
				.toList();

		int total = todos.size();
		int completed = (int)todos.stream()
			.filter(SessionResponseDTO.SessionTodoResponseDTO::getIsCompleted)
			.count();

		Integer achievementRate = (total == 0) ? 0 : (int)((completed * 100.0) / total);

		SessionResponseDTO.SessionTaskResponseDTO taskDTO =
			SessionConverter.toSessionTaskResponseDTO(task, todos);

		return SessionConverter.toInProgressMemberResponseDTO(
			member,
			srm,
			taskDTO,
			achievementRate
		);
	}
}
