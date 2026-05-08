package com.example.gak.domain.chat.service;

import static com.example.gak.domain.chat.converter.ChatConverter.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gak.domain.chat.dto.ChatRequestDTO;
import com.example.gak.domain.chat.entity.ChatMessage;
import com.example.gak.domain.chat.repository.ChatMessageRepository;
import com.example.gak.domain.member.entity.Member;
import com.example.gak.domain.member.repository.MemberRepository;
import com.example.gak.domain.session.entity.SessionRoom;
import com.example.gak.domain.session.repository.SessionRoomRepository;
import com.example.gak.global.redis.RedisPublisher;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatCommandService {

	private final RedisPublisher redisPublisher;
	private final ChatMessageRepository chatMessageRepository;
	private final SessionRoomRepository sessionRoomRepository;
	private final MemberRepository memberRepository;

	public void sendMessage(Long sessionId, Long memberId, ChatRequestDTO.SendChatMessageRequestDTO dto) {
		SessionRoom sessionRoom = sessionRoomRepository.getReferenceById(sessionId);
		Member member = memberRepository.getReferenceById(memberId);

		redisPublisher.chatMessagePublish(
			sessionId,
			toChatMessageResponseDTO(
				memberId,
				dto.getContent(),
				dto.getType(),
				dto.getQuickActionType()));

		chatMessageRepository.save(new ChatMessage(
			dto.getContent(),
			dto.getType(),
			dto.getQuickActionType(),
			sessionRoom,
			member
		));
	}
}
