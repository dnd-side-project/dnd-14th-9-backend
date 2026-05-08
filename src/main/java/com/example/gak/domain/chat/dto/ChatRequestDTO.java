package com.example.gak.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
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
		@Size(min = 1, max = 200)
		private String content;

		@NotNull
		private ChatMessageType type;

		private QuickActionType quickActionType;

		@AssertTrue(message = "일반 채팅은 content가 필요합니다.")
		public boolean isValidTextMessage() {
			return type != ChatMessageType.TEXT || (content != null && !content.isBlank());
		}

		@AssertTrue(message = "퀵메시지는 quickActionType이 필요합니다.")
		public boolean isValidQuickActionMessage() {
			return type != ChatMessageType.QUICK_ACTION || quickActionType != null;
		}
	}
}