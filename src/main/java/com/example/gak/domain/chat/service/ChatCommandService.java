package com.example.gak.domain.chat.service;

import static com.example.gak.domain.chat.converter.ChatConverter.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.chat.dto.ChatRequestDTO;
import com.example.gak.global.redis.RedisPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatCommandService {

	private final RedisPublisher redisPublisher;

	public void sendMessage(Long sessionId, Long memberId, ChatRequestDTO.SendChatMessageRequestDTO dto) {
		redisPublisher.chatMessagePublish(
			sessionId,
			toChatMessageResponseDTO(memberId, dto.getContent()));
	}
}