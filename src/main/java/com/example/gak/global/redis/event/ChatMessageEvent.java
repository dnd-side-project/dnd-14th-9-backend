package com.example.gak.global.redis.event;

import com.example.gak.domain.chat.dto.ChatResponseDTO;

public record ChatMessageEvent(Long sessionId, ChatResponseDTO.ChatMessageResponseDTO dto) {
}
