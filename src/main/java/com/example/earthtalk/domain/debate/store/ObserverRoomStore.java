package com.example.earthtalk.domain.debate.store;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class ObserverRoomStore {

	private final ConcurrentHashMap<String, Set<String>> roomObservers = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, AtomicInteger> maxObserverCountMap = new ConcurrentHashMap<>();

	public void addUser(String roomId, String userName) {
		Set<String> observers = roomObservers.computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet());
		observers.add(userName);
		int currentCount = observers.size();

		maxObserverCountMap.compute(roomId, (key, currentMax) -> {
			if (currentMax == null || currentCount > currentMax.get()) {
				return new AtomicInteger(currentCount);
			} else {
				return currentMax;
			}
		});

	}

	public void removeUser(String roomId, String userName) {
		Set<String> observers = roomObservers.get(roomId);
		if (observers != null) {
			observers.remove(userName);
			if (observers.isEmpty()) {
				roomObservers.remove(roomId);
			}
		}
	}

	public int getObserverCount(String roomId) {
		return roomObservers.getOrDefault(roomId, Collections.emptySet()).size();
	}

	public int getMaxObserverCount(String roomId) {
		AtomicInteger max = maxObserverCountMap.get(roomId);
		return max == null ? 0 : max.get();
	}
}
