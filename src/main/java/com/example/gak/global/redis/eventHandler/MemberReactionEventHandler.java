package com.example.gak.global.redis.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.MemberReactionUpdateEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberReactionEventHandler {

	private final RedisPublisher redisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MemberReactionUpdateEvent event) {
		redisPublisher.memberReactionPublish(event.sessionId(), event.memberId());
	}
}