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
		@Size(max = 200, message = "채팅 내용은 200자 이하로 입력해주세요.")
		private String content;

		@NotNull(message = "메시지 유형을 선택해주세요.")
		private ChatMessageType type;

		private QuickActionType quickActionType;

		@AssertTrue(message = "채팅 내용을 입력해주세요.")
		public boolean isValidTextMessage() {
			return type != ChatMessageType.TEXT || (content != null && !content.isBlank());
		}

		@AssertTrue(message = "퀵 액션을 선택해주세요.")
		public boolean isValidQuickActionMessage() {
			return type != ChatMessageType.QUICK_ACTION || quickActionType != null;
		}
	}
}