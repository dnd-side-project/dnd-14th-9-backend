package com.example.gak.domain.chat.converter;

import com.example.gak.domain.chat.dto.ChatResponseDTO;
import com.example.gak.domain.chat.dto.QuickActionType;
import com.example.gak.domain.chat.dto.ChatMessageType;

public class ChatConverter {

	public static ChatResponseDTO.ChatMessageResponseDTO toChatMessageResponseDTO(
		Long memberId,
		String content,
		ChatMessageType type,
		QuickActionType quickActionType
	) {
		return ChatResponseDTO.ChatMessageResponseDTO.builder()
			.memberId(memberId)
			.content(content)
			.type(type)
			.quickActionType(quickActionType)
			.build();
	}
}

