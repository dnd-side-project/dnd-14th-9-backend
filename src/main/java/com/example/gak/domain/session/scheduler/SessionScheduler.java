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
			log.info("sessionStartScheduler 대상 세션 수: {}", sessionsToStart.size());

			for (Long sessionId : sessionsToStart) {
				try {
					long start = System.currentTimeMillis();
					sessionCommandService.startSession(sessionId);
					log.info("startSession({}) {}ms", sessionId, System.currentTimeMillis() - start);
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
			List<Long> sessionsToEnd = sessionQueryService.findSessionsToEnd();
			log.info("sessionEndScheduler 대상 세션 수: {}", sessionsToEnd.size());

			for (Long sessionId : sessionsToEnd) {
				try {
					long start = System.currentTimeMillis();
					sessionCommandService.endSession(sessionId);
					log.info("endSession({}) {}ms", sessionId, System.currentTimeMillis() - start);
				} catch (Exception e) {
					log.error("세션 종료 처리 실패함: {}", sessionId, e);
				}
			}
		} catch (Exception e) {
			log.error("Failed to execute session scheduler", e);
		}
	}
}