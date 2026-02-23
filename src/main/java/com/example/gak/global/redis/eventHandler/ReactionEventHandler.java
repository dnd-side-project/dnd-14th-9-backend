package com.example.gak.global.redis.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.ReactionUpdateEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReactionEventHandler {

	private final RedisPublisher redisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(ReactionUpdateEvent event) {
		redisPublisher.reactionPublish(event.sessionId());
	}
}
