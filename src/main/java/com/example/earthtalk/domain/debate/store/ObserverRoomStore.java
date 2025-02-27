package com.example.earthtalk.domain.debate.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObserverRoomStore {

	private static final String OBSERVER_KEY_PREFIX = "debate:observers:";

	private static final String MAX_OBSERVER_KEY = "debate:observers:max";

	private final StringRedisTemplate redisTemplate;



	private final ConcurrentHashMap<String, Set<String>> roomObservers = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, AtomicInteger> maxObserverCountMap = new ConcurrentHashMap<>();

	public void addUser(String roomId, String userName) {
		validateRoomIdAndUserId(roomId, userName);
		String key = OBSERVER_KEY_PREFIX + roomId;

		redisTemplate.opsForSet().add(key, userName);

		Long currentCount = redisTemplate.opsForSet().size(key);

		Object currentMaxObj = redisTemplate.opsForHash().get(MAX_OBSERVER_KEY, roomId);
		int currentMax = (currentMaxObj != null) ? Integer.parseInt(currentMaxObj.toString()) : 0;

		if (currentCount != null && currentCount > currentMax) {
			redisTemplate.opsForSet().remove(MAX_OBSERVER_KEY, roomId, currentCount.toString());
		}
	}

	public void removeUser(String roomId, String userName) {
		validateRoomIdAndUserId(roomId, userName);
		String key = OBSERVER_KEY_PREFIX + roomId;
		redisTemplate.opsForSet().remove(key, userName);
		Long size = redisTemplate.opsForSet().size(key);

		if (size == null || size == 0) {
			redisTemplate.delete(key);
			redisTemplate.opsForHash().delete(MAX_OBSERVER_KEY, roomId);
		}

	}

	public int getObserverCount(String roomId) {
		String key = OBSERVER_KEY_PREFIX + roomId;
		Long size = redisTemplate.opsForSet().size(key);
		return (size != null) ? size.intValue() : 0;
	}

	public int getMaxObserverCount(String roomId) {
		Object value = redisTemplate.opsForHash().get(MAX_OBSERVER_KEY, roomId);
		return (value != null) ? Integer.parseInt(value.toString()) : 0;
	}

	public Map<String, Integer> getObserverCounts() {
		Map<String, Integer> observerCounts = new HashMap<>();
		Set<String> keys = redisTemplate.keys(OBSERVER_KEY_PREFIX + "*");
		if (keys != null) {
			for (String key : keys) {
				Long size = redisTemplate.opsForSet().size(key);
				String roomId = key.substring(OBSERVER_KEY_PREFIX.length());
				observerCounts.put(roomId, (size == null) ? 0 : size.intValue());
			}
		}
		return observerCounts;
	}

	private void validateRoomIdAndUserId(String roomId, String userName) {
		if (roomId == null || roomId.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
		}
		if (userName == null || userName.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage());
		}
	}
}
