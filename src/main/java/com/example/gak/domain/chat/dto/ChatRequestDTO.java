package com.example.gak.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRequestDTO {

	@Schema(name = "채팅 전송 요청")
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SendChatMessageRequestDTO {
		@NotBlank
		@Size(min = 1, max = 200)
		private String content;
	}
}