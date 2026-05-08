package com.example.gak.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class ChatResponseDTO {

	@Schema(name = "채팅 메시지 응답")
	@Builder
	@Getter
	public static class ChatMessageResponseDTO {
		private Long memberId;
		private String content;
		private ChatMessageType type;
		private QuickActionType quickActionType;
	}
}
