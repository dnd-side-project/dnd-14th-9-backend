package com.example.gak.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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

		private QuickActionType quickActionType;

		@AssertTrue(message = "채팅 내용을 입력해주세요.")
		public boolean isValidContent() {
			return quickActionType != null || (content != null && !content.isBlank());
		}
	}
}