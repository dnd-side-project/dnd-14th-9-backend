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

	@Scheduled(cron = "0 * * * * *")
	public void sessionStartScheduler() {
		try {
			List<Long> sessionsToStart = sessionQueryService.findSessionsToStart();

			for (Long sessionId : sessionsToStart) {
				try {
					sessionCommandService.startSession(sessionId);
				} catch (Exception e) {
					log.error("세션 시작 처리 실패함: {}", sessionId, e);
				}
			}
		} catch (Exception e) {
			log.error("Failed to execute session scheduler", e);
		}
	}

	@Scheduled(cron = "0 * * * * *")
	public void sessionEndScheduler() {
		try {
			List<Long> sessionsToStart = sessionQueryService.findSessionsToEnd();

			for (Long sessionId : sessionsToStart) {
				try {
					sessionCommandService.endSession(sessionId);
				} catch (Exception e) {
					log.error("세션 종료 처리 실패함: {}", sessionId, e);
				}
			}
		} catch (Exception e) {
			log.error("Failed to execute session scheduler", e);
		}
	}
}