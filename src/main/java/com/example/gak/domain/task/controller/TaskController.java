package com.example.gak.domain.task.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.task.dto.SubTaskRequestDTO;
import com.example.gak.domain.task.dto.TaskRequestDTO;
import com.example.gak.domain.task.service.TaskCommandService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "목표 API")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

	private final TaskCommandService taskCommandService;

	@Operation(summary = "목표 수정 API")
	@PatchMapping("/{taskId}")
	public ApiResponse<Void> updateTask(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@PathVariable Long taskId,
		@RequestBody @Valid TaskRequestDTO.UpdateTaskDTO request
	) {
		taskCommandService.updateTask(taskId, oAuth2User.getMemberId(), request);
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "TODO 추가 API")
	@PostMapping("/{taskId}/subtasks")
	public ApiResponse<List<SessionResponseDTO.todoResponseDTO>> addSubTasks(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@PathVariable Long taskId,
		@RequestBody List<SubTaskRequestDTO.AddSubTaskDTO> request
	) {
		return ApiResponse.onSuccess(taskCommandService.addSubtasks(taskId, oAuth2User.getMemberId(), request));
	}
}
