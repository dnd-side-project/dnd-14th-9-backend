package com.example.gak.global.webhook.eventHandler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.gak.global.webhook.WebhookService;
import com.example.gak.global.webhook.event.MemberSignedUpEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberSignedUpEventHandler {

	private final WebhookService webhookService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MemberSignedUpEvent event) {
		webhookService.sendDiscordNotification();
	}
}
