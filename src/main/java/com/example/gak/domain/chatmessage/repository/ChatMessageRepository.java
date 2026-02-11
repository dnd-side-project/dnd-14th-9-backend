package com.example.gak.domain.chatmessage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gak.domain.chatmessage.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	void deleteByMemberId(Long memberId);
}
