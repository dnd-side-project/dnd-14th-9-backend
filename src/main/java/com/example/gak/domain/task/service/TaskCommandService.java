package com.example.gak.domain.task.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.task.dto.SubTaskRequestDTO;
import com.example.gak.domain.task.entity.SubTask;
import com.example.gak.domain.task.repository.SubTaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskCommandService {

	private final SubTaskRepository subTaskRepository;

	public void toggleSubTaskCompletion(Long subTaskId, Long memberId) {

		SubTask subTask = subTaskRepository.findById(subTaskId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SUBTASK_NOT_FOUND));

		if (!subTask.getTask().getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_ACCESS_DENIED);
		}

		if (subTask.getTask().getSessionRoom().getStatus() != SessionRoomStatus.IN_PROGRESS) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_COMPLETION_NOT_ALLOWED);
		}

		subTask.toggleCompletion();
	}

	public void updateSubtask(
		Long subtaskId,
		Long memberId,
		SubTaskRequestDTO.UpdateSubTaskDTO request
	) {
		SubTask subTask = subTaskRepository.findById(subtaskId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SUBTASK_NOT_FOUND));

		if (!subTask.getTask().getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_ACCESS_DENIED);
		}

		if (subTask.getTask().getSessionRoom().getStatus() != SessionRoomStatus.WAITING) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_COMPLETION_NOT_ALLOWED);
		}

		subTask.updateSubTaskTitle(request.getTodoContent());
	}
}
