package com.example.gak.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class TaskRequestDTO {

	@Schema(name = "세션 목표 수정 요청")
	@Getter
	@Builder
	public static class UpdateTaskDTO {
		private String goalContent;
	}
}
