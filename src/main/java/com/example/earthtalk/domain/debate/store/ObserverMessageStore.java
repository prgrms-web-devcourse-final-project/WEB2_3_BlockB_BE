package com.example.earthtalk.domain.debate.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.dto.ObserverMessage;

@Component
public class ObserverMessageStore {

	private final Map<String, List<ObserverMessage>> observerMessagesMap = new ConcurrentHashMap<>();

	public List<ObserverMessage> getOrCreateObserverMessages(String roomId) {
		return observerMessagesMap.computeIfAbsent(roomId, k -> new ArrayList<>());
	}

	public void addObserverMessage(String roomId, ObserverMessage message) {
		getOrCreateObserverMessages(roomId).add(message);
	}

	public List<ObserverMessage> removeObserverMessages(String roomId) {
		return observerMessagesMap.remove(roomId);
	}

	public Map<String, List<ObserverMessage>> getAllObserverMessages() {
		return observerMessagesMap;
	}
}
