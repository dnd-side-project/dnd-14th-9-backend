package com.example.gak.global.redis.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.redis.RedisPublisher;
import com.example.gak.global.redis.event.InProgressRoomUpdateEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InProgressRoomEventHandler {

	private final RedisPublisher redisPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(InProgressRoomUpdateEvent event) {
		redisPublisher.inProgressRoomPublish(event.sessionId());
	}
}