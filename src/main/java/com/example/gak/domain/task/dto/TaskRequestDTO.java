package com.example.gak.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class TaskRequestDTO {

	@Schema(name = "세션 목표 수정 요청")
	@Getter
	@Builder
	public static class UpdateTaskDTO {
		@NotNull
		@Size(max = 50)
		private String goalContent;
	}
}
