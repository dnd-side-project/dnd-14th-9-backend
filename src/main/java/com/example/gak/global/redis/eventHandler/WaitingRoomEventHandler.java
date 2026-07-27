package com.example.gak.global.redis.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.WaitingRoomUpdateEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WaitingRoomEventHandler {

	private final RedisPublisher redisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(WaitingRoomUpdateEvent event) {
		redisPublisher.waitingRoomPublish(event.sessionId());
	}
}