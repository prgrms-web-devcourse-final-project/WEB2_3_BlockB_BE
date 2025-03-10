package com.example.earthtalk.domain.debate.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObserverRoomStore {

	private static final String OBSERVER_KEY_PREFIX = "debate:observers:";

	private static final String MAX_OBSERVER_KEY = "debate:observers:max";

	private static final String OBSERVER_CURRENT_ZSET_KEY = "debate:observers:score:current";

	private static final String OBSERVER_MAX_ZSET_KEY = "debate:observers:score:max";

	private final StringRedisTemplate redisTemplate;

	public void addUser(String roomId, String userName) {
		validateRoomIdAndUserId(roomId, userName);
		String key = OBSERVER_KEY_PREFIX + roomId;

		redisTemplate.opsForSet().add(key, userName);

		Long currentCount = redisTemplate.opsForSet().size(key);

		Object currentMaxObj = redisTemplate.opsForHash().get(MAX_OBSERVER_KEY, roomId);
		int currentMax = (currentMaxObj != null) ? Integer.parseInt(currentMaxObj.toString()) : 0;

		if (currentCount != null && currentCount > currentMax) {
			redisTemplate.opsForHash().put(MAX_OBSERVER_KEY, roomId, currentCount.toString());
		}

		updateObserverScore(roomId);
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
		updateObserverScore(roomId);
	}

	public Long getObserverCount(String roomId) {
		String key = OBSERVER_KEY_PREFIX + roomId;
		Long size = redisTemplate.opsForSet().size(key);
		return (size != null) ? size : 0L;
	}

	public Long getMaxObserverCount(String roomId) {
		Object value = redisTemplate.opsForHash().get(MAX_OBSERVER_KEY, roomId);
		return (value != null) ? Long.parseLong(value.toString()) : 0L;
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

	private void updateObserverScore(String roomId) {
		Long currentCount = getObserverCount(roomId);
		redisTemplate.opsForZSet().add(OBSERVER_CURRENT_ZSET_KEY, roomId, currentCount);
		Long maxCount = getMaxObserverCount(roomId);
		redisTemplate.opsForZSet().add(OBSERVER_MAX_ZSET_KEY, roomId, maxCount);
	}

	public void initializeRoom(String roomId) {
		redisTemplate.opsForZSet().add(OBSERVER_CURRENT_ZSET_KEY, roomId, 0);
		redisTemplate.opsForZSet().add(OBSERVER_MAX_ZSET_KEY, roomId, 0);
	}


	private void validateRoomIdAndUserId(String roomId, String userName) {
		if (roomId == null || roomId.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
		}
		if (userName == null || userName.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage());
		}
	}

	public List<String> getSortedRoomIdsByCurrentViewer() {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
		Set<String> sortedRoomIds = zSetOps.reverseRange(OBSERVER_CURRENT_ZSET_KEY, 0, -1);
		return (sortedRoomIds != null) ? new ArrayList<>(sortedRoomIds) : Collections.emptyList();
	}

	public List<String> getSortedRoomIdsByMaxViewer() {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
		Set<String> sortedRoomIds = zSetOps.reverseRange(OBSERVER_MAX_ZSET_KEY, 0, -1);
		return (sortedRoomIds != null) ? new ArrayList<>(sortedRoomIds) : Collections.emptyList();
	}

}
