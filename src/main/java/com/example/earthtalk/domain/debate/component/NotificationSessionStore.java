package com.example.earthtalk.domain.debate.component;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSessionStore {
    private final Map<Long, String> userToSessionMap = new ConcurrentHashMap<>();

    public void registerSession(Long userId, String sessionId) {
        userToSessionMap.put(userId, sessionId);
    }

    public String getSession(Long userId) {
        return userToSessionMap.get(userId);
    }

    public boolean hasSession(String sessionId) {
        return userToSessionMap.containsValue(sessionId);
    }

    public void removeSessionBySessionId(String sessionId) {
        userToSessionMap.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
    }

    public void removeSessionByUserId(Long userId) {
        userToSessionMap.remove(userId);
    }
}
