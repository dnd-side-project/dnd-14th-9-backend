package com.example.gak.domain.session.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.gak.domain.session.service.SessionCommandService;
import com.example.gak.domain.session.service.SessionQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionScheduler {

	private final SessionCommandService sessionCommandService;
	private final SessionQueryService sessionQueryService;

	@Scheduled(fixedDelay = 60000)
	public void run() {

		List<Long> sessionsToStart = sessionQueryService.findSessionsToStart();

		for (Long sessionId : sessionsToStart) {
			sessionCommandService.startSession(sessionId);
		}
	}
}
