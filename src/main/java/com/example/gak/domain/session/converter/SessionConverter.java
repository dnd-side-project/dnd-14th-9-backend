package com.example.gak.domain.session.converter;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.session.dto.SessionRequestDTO;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.entity.SessionRoomMember;
import com.example.gak.domain.task.entity.SubTask;

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
			.build();
	}

	public static SessionResponseDTO.joinSessionResponseDTO tojoinSessionResponseDTO(
		SessionRoomMember sessionRoomMember,
		Member member,
		SessionRoom sessionRoom,
		SessionRequestDTO.SessionJoinRequestDTO request,
		List<SessionResponseDTO.todoResponseDTO> todos
	) {
		return SessionResponseDTO.joinSessionResponseDTO.builder()
			.memberId(member.getId())
			.sessionId(sessionRoom.getId())
			.role(sessionRoomMember.getRole())
			.goal(request.getGoal())
			.todos(todos)
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
}
