package com.example.gak.domain.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class SubTaskRequestDTO {

	@Schema(name = "세션 TODO 수정 요청")
	@Getter
	@Builder
	public static class UpdateSubTaskDTO {
		private String todoContent;
	}
}
