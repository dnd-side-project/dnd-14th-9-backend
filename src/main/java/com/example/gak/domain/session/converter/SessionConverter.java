package com.example.gak.domain.session.converter;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.record.entity.Record;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.task.entity.SubTask;
import com.example.gak.domain.task.entity.Task;

public class SessionConverter {

	public static SessionResponseDTO.SessionCardResponseDTO toSessionCardResponseDTO(SessionRoom sessionRoom) {
		return SessionResponseDTO.SessionCardResponseDTO.builder()
			.sessionId(sessionRoom.getId())
			.category(sessionRoom.getCategory().getDisplayName())
			.title(sessionRoom.getTitle())
			.hostNickname(sessionRoom.getMember().getNickname())
			.imageUrl(sessionRoom.getThumbnailImageUrl())
			.currentParticipants(sessionRoom.getCurrentCount())
			.maxParticipants(sessionRoom.getMaxCapacity())
			.startTime(sessionRoom.getStartTime())
			.sessionDurationMinutes(sessionRoom.getDurationMinutes())
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

	public static SessionResponseDTO.CreateSessionResponseDTO toCreateSessionResponseDTO(
		SessionRoom sessionRoom
	) {
		return SessionResponseDTO.CreateSessionResponseDTO.builder()
			.createdSessionId(sessionRoom.getId())
			.build();
	}

	public static SessionResponseDTO.SessionDetailResponseDTO toSessionDetailResponseDTO(
		SessionRoom sessionRoom
	) {
		return SessionResponseDTO.SessionDetailResponseDTO.builder()
			.sessionId(sessionRoom.getId())
			.category(sessionRoom.getCategory().getDisplayName())
			.title(sessionRoom.getTitle())
			.hostNickname(sessionRoom.getMember().getNickname())
			.imageUrl(sessionRoom.getThumbnailImageUrl())
			.currentParticipants(sessionRoom.getCurrentCount())
			.maxParticipants(sessionRoom.getMaxCapacity())
			.sessionDurationMinutes(sessionRoom.getDurationMinutes())
			.startTime(sessionRoom.getStartTime())
			.status(sessionRoom.getStatus().getDisplayName())

			.summary(sessionRoom.getSummary())
			.notice(sessionRoom.getNotice())

			.requiredFocusRate(sessionRoom.getRequiredFocusRate())
			.requiredAchievementRate(sessionRoom.getRequiredAchievementRate())
			.build();
	}

	public static SessionResponseDTO.joinSessionResponseDTO tojoinSessionResponseDTO(
		SessionRoomMember sessionRoomMember,
		Member member,
		SessionRoom sessionRoom,
		SessionResponseDTO.taskResponseDTO task
	) {
		return SessionResponseDTO.joinSessionResponseDTO.builder()
			.memberId(member.getId())
			.sessionId(sessionRoom.getId())
			.role(sessionRoomMember.getRole())
			.task(task)
			.build();
	}

	public static SessionResponseDTO.todoResponseDTO toTodoResponseDTO(
		SubTask subTask
	) {
		return SessionResponseDTO.todoResponseDTO.builder()
			.subtaskId(subTask.getId())
			.content(subTask.getSubTaskTitle())
			.build();
	}

	public static SessionResponseDTO.taskResponseDTO toTaskResponseDTO(
		List<SessionResponseDTO.todoResponseDTO> todos,
		Task task
	) {
		return SessionResponseDTO.taskResponseDTO.builder()
			.goal(task.getGoal())
			.taskId(task.getId())
			.todos(todos)
			.build();
	}

	public static SessionResponseDTO.WaitingMemberResponseDTO toWaitingMemberResponseDTO(
		Member member,
		SessionRoomMember sessionRoomMember,
		SessionResponseDTO.taskResponseDTO task,
		Record record
	) {
		return SessionResponseDTO.WaitingMemberResponseDTO.builder()
			.nickname(member.getNickname())
			.memberId(member.getId())
			.profileImageUrl(member.getProfileImageUrl())
			.achievementRate(record.getTodoCompletionRate())
			.focusRate(record.getFocusRate())
			.role(sessionRoomMember.getRole())
			.task(task)
			.build();
	}

	public static SessionResponseDTO.WaitingResponseDTO toWaitingResponseDTO(
		int participantCount,
		List<SessionResponseDTO.WaitingMemberResponseDTO> members
	) {
		return SessionResponseDTO.WaitingResponseDTO.builder()
			.participantCount(participantCount)
			.members(members)
			.build();
	}

	public static SessionResponseDTO.ToggleSessionMemberStatusResponseDTO toToggleSessionMemberStatusResponseDTO(
		SessionRoomMember sessionRoomMember
	) {
		return SessionResponseDTO.ToggleSessionMemberStatusResponseDTO.builder()
			.currentStatus(sessionRoomMember.getStatus())
			.build();
	}

	public static SessionResponseDTO.SessionTodoResponseDTO toSessionTodoResponseDTO(
		SubTask subTask
	) {
		return SessionResponseDTO.SessionTodoResponseDTO.builder()
			.subtaskId(subTask.getId())
			.content(subTask.getSubTaskTitle())
			.isCompleted(subTask.isCompleted())
			.build();
	}

	public static SessionResponseDTO.SessionTaskResponseDTO toSessionTaskResponseDTO(
		Task task,
		List<SessionResponseDTO.SessionTodoResponseDTO> sessionTodoResponseDTOS
	) {
		return SessionResponseDTO.SessionTaskResponseDTO.builder()
			.taskId(task.getId())
			.goal(task.getGoal())
			.todos(sessionTodoResponseDTOS)
			.build();
	}

	public static SessionResponseDTO.InProgressMemberResponseDTO toInProgressMemberResponseDTO(
		Member member,
		SessionRoomMember sessionRoomMember,
		SessionResponseDTO.SessionTaskResponseDTO task,
		Integer achievementRate
	) {
		return SessionResponseDTO.InProgressMemberResponseDTO.builder()
			.memberId(member.getId())
			.nickname(member.getNickname())
			.role(sessionRoomMember.getRole())
			.profileImageUrl(member.getProfileImageUrl())
			.achievementRate(achievementRate)
			.status(sessionRoomMember.getStatus())
			.task(task)
			.build();
	}

	public static SessionResponseDTO.InProgressResponseDTO toInProgressResponseDTO(
		int participantCount,
		int averageAchievementRate,
		List<SessionResponseDTO.InProgressMemberResponseDTO> members
	) {
		return SessionResponseDTO.InProgressResponseDTO.builder()
			.participantCount(participantCount)
			.averageAchievementRate(averageAchievementRate)
			.members(members)
			.build();
	}

	public static SessionResponseDTO.SessionStartResponseDTO toSessionStartResponseDTO(
		SessionRoom sessionRoom
	) {
		return SessionResponseDTO.SessionStartResponseDTO.builder()
			.status(sessionRoom.getStatus())
			.build();
	}
}
