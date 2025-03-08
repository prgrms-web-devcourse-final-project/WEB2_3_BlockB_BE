package com.example.earthtalk.domain.debate.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketIdleSessionMonitor {

	private static final long IDLE_TIMEOUT = 10 * 60 * 1000;

	private final Map<String, Long> sessionActivityMap = new ConcurrentHashMap<>();

	private final SessionRegistry sessionRegistry;

	public void registerSession(String sessionId) {
		sessionActivityMap.put(sessionId, System.currentTimeMillis());
	}

	public void updateSessionActivity(String sessionId) {
		sessionActivityMap.put(sessionId, System.currentTimeMillis());
	}

	public void unregisterSession(String sessionId) {
		sessionActivityMap.remove(sessionId);

		sessionRegistry.removeSession(sessionId);
	}

	@Scheduled(fixedRate = 60000)
	public void checkIdleSessions() {
		long now = System.currentTimeMillis();
		for (Map.Entry<String, Long> entry : sessionActivityMap.entrySet()) {
			if (now - entry.getValue() > IDLE_TIMEOUT) {
				String sessionId = entry.getKey();
				closeSession(sessionId);
				sessionActivityMap.remove(sessionId);
			}
		}
	}

	private void closeSession(String sessionId) {
		WebSocketSession session = sessionRegistry.getSession(sessionId);
		if (session != null && session.isOpen()) {
			try {
				session.close();
			} catch (Exception e) {
				throw new RuntimeException(ErrorCode.SESSION_DISCONNECT_FAILED.getMessage());
			}
		}
		sessionRegistry.removeSession(sessionId);
		log.info("10분 동안 활동이 없어 종료하는 세션: {}", sessionId);
	}
}
