package com.example.gak.global.redis.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.ChatMessageEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageEventHandler {

	private final RedisPublisher redisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ChatMessageEvent event) {
		try {
			redisPublisher.chatMessagePublish(event.sessionId(), event.dto());
		} catch (Exception e) {
			log.error("채팅 메시지 Redis 발행 실패 - sessionId: {}, memberId: {}", event.sessionId(), event.dto().getMemberId(), e);
		}
	}
}
