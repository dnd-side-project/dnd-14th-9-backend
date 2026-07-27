package com.example.gak.domain.chat.converter;

import com.example.gak.domain.chat.dto.ChatResponseDTO;
import com.example.gak.domain.chat.dto.QuickActionType;

public class ChatConverter {

	public static ChatResponseDTO.ChatMessageResponseDTO toChatMessageResponseDTO(
		Long memberId,
		String content,
		QuickActionType quickActionType
	) {
		return ChatResponseDTO.ChatMessageResponseDTO.builder()
			.memberId(memberId)
			.content(content)
			.quickActionType(quickActionType)
			.build();
	}
}

