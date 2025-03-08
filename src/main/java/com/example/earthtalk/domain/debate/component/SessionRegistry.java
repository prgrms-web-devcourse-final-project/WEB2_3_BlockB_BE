package com.example.earthtalk.domain.debate.component;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

	public void registerSession(WebSocketSession session) {
		sessions.put(session.getId(), session);
	}

	public WebSocketSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}

	public void removeSession(String sessionId) {
		sessions.remove(sessionId);
	}
}
