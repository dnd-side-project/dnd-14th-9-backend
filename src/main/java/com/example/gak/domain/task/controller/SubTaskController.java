package com.example.gak.domain.task.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gak.domain.session.dto.SessionResponseDTO;
import com.example.gak.domain.task.dto.SubTaskRequestDTO;
import com.example.gak.domain.task.service.TaskCommandService;
import com.example.gak.global.apiPayload.ApiResponse;
import com.example.gak.global.security.oauth2.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "TODO API")
@RestController
@RequestMapping("/api/v1/subtasks")
@RequiredArgsConstructor
public class SubTaskController {

	private final TaskCommandService taskCommandService;

	@Operation(summary = "TODO 완료 상태 토글 API")
	@PatchMapping("/{subTaskId}/completion")
	public ApiResponse<Void> toggleSubTaskCompletion(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@PathVariable Long subTaskId
	) {
		taskCommandService.toggleSubTaskCompletion(subTaskId, oAuth2User.getMemberId());
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "TODO 추가 API")
	@PostMapping
	public ApiResponse<List<SessionResponseDTO.todoResponseDTO>> addSubTask(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@RequestParam Long taskId,
		@RequestBody List<SubTaskRequestDTO.AddSubTaskDTO> request
	) {
		return ApiResponse.onSuccess(taskCommandService.addSubtask(taskId, oAuth2User.getMemberId(), request));
	}

	@Operation(summary = "TODO 수정 API")
	@PatchMapping("/{subTaskId}")
	public ApiResponse<Void> updateSubTask(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@PathVariable Long subTaskId,
		@RequestBody SubTaskRequestDTO.UpdateSubTaskDTO request
	) {
		taskCommandService.updateSubtask(subTaskId, oAuth2User.getMemberId(), request);
		return ApiResponse.onSuccess(null);
	}

	@Operation(summary = "TODO 삭제 API")
	@DeleteMapping("/{subTaskId}")
	public ApiResponse<Void> updateSubTask(
		@AuthenticationPrincipal CustomOAuth2User oAuth2User,
		@PathVariable Long subTaskId
	) {
		taskCommandService.deleteSubtask(subTaskId, oAuth2User.getMemberId());
		return ApiResponse.onSuccess(null);
	}
}
