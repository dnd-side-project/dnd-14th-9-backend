package com.example.gak.domain.task.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.session.converter.SessionConverter;
import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.session.entity.enums.SessionRoomStatus;
import com.example.gak.domain.task.dto.SubTaskRequestDTO;
import com.example.gak.domain.task.dto.TaskRequestDTO;
import com.example.gak.domain.task.entity.SubTask;
import com.example.gak.domain.task.entity.Task;
import com.example.gak.domain.task.repository.SubTaskRepository;
import com.example.gak.domain.task.repository.TaskRepository;
import com.example.gak.global.apiPayload.code.GeneralErrorCode;
import com.example.gak.global.apiPayload.exception.GeneralException;
import com.example.gak.global.redis.event.InProgressRoomUpdateEvent;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskCommandService {

	private final SubTaskRepository subTaskRepository;
	private final TaskRepository taskRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	public void toggleSubTaskCompletion(Long subTaskId, Long memberId) {

		SubTask subTask = subTaskRepository.findById(subTaskId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SUBTASK_NOT_FOUND));

		if (!subTask.getTask().getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_ACCESS_DENIED);
		}

		if (subTask.getTask().getSessionRoom().getStatus() != SessionRoomStatus.IN_PROGRESS) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_UPDATE_NOT_ALLOWED);
		}

		subTask.toggleCompletion();

		applicationEventPublisher.publishEvent(
			new InProgressRoomUpdateEvent(subTask.getTask().getSessionRoom().getId())
		);
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
			throw new GeneralException(GeneralErrorCode.SUBTASK_UPDATE_NOT_ALLOWED);
		}

		subTask.updateSubTaskTitle(request.getTodoContent());
	}

	public void updateTask(
		Long taskId,
		Long memberId,
		TaskRequestDTO.UpdateTaskDTO request
	) {
		Task task = taskRepository.findById(taskId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.TASK_NOT_FOUND));

		if (!task.getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.TASK_ACCESS_DENIED);
		}

		if (task.getSessionRoom().getStatus() != SessionRoomStatus.WAITING) {
			throw new GeneralException(GeneralErrorCode.TASK_UPDATE_NOT_ALLOWED);
		}

		task.updateGoal(request.getGoalContent());
	}

	public void deleteSubtask(
		Long subtaskId,
		Long memberId
	) {
		SubTask subTask = subTaskRepository.findById(subtaskId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.SUBTASK_NOT_FOUND));

		if (!subTask.getTask().getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_ACCESS_DENIED);
		}

		if (subTask.getTask().getSessionRoom().getStatus() != SessionRoomStatus.WAITING) {
			throw new GeneralException(GeneralErrorCode.SUBTASK_UPDATE_NOT_ALLOWED);
		}

		subTaskRepository.delete(subTask);
	}

	public List<SessionResponseDTO.todoResponseDTO> addSubtasks(
		Long taskId,
		Long memberId,
		List<SubTaskRequestDTO.AddSubTaskDTO> request
	) {
		Task task = taskRepository.findById(taskId)
			.orElseThrow(() -> new GeneralException(GeneralErrorCode.TASK_NOT_FOUND));

		if (!task.getMember().getId().equals(memberId)) {
			throw new GeneralException(GeneralErrorCode.TASK_ACCESS_DENIED);
		}

		if (task.getSessionRoom().getStatus() != SessionRoomStatus.WAITING) {
			throw new GeneralException(GeneralErrorCode.TASK_ACCESS_DENIED);
		}

		List<SubTask> subTasks = request.stream()
			.map(todo -> new SubTask(todo.getTodoContent(), task))
			.toList();

		subTaskRepository.saveAll(subTasks);

		return subTasks.stream()
			.map(SessionConverter::toTodoResponseDTO)
			.toList();
	}
}
