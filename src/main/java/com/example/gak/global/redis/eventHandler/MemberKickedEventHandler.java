package com.example.gak.global.redis.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.MemberKickedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberKickedEventHandler {

	private final RedisPublisher redisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MemberKickedEvent event) {
		redisPublisher.kickedPublish(event.sessionId(), event.memberIds());
	}
}
