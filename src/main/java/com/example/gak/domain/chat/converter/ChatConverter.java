package com.example.gak.domain.chat.converter;

import com.example.gak.domain.chat.dto.ChatResponseDTO;

public class ChatConverter {

	public static ChatResponseDTO.ChatMessageResponseDTO toChatMessageResponseDTO(
		Long memberId,
		String content
	) {
		return ChatResponseDTO.ChatMessageResponseDTO.builder()
			.memberId(memberId)
			.message(content)
			.build();
	}
}

