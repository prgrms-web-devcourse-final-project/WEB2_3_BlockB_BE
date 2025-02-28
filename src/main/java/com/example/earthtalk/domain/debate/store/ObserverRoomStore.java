package com.example.earthtalk.domain.debate.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.dto.DebateObserverResponse;
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

	private final DebateRoomStore debateRoomStore;

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

	private void updateObserverScore(String roomId) {
		int currentCount = getObserverCount(roomId);
		redisTemplate.opsForZSet().add(OBSERVER_CURRENT_ZSET_KEY, roomId, currentCount);
		int maxCount = getMaxObserverCount(roomId);
		redisTemplate.opsForZSet().add(OBSERVER_MAX_ZSET_KEY, roomId, maxCount);
	}


	private void validateRoomIdAndUserId(String roomId, String userName) {
		if (roomId == null || roomId.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
		}
		if (userName == null || userName.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage());
		}
	}

	public List<DebateObserverResponse> getAllDebateObserverResponsesSortedByCurrentDesc() {
		Set<String> roomIds = redisTemplate.opsForZSet()
			.reverseRange(OBSERVER_CURRENT_ZSET_KEY, 0, -1);
		return buildDebateObserverResponseList(roomIds);
	}

	public List<DebateObserverResponse> getAllDebateObserverResponsesSortedByMaxDesc() {
		Set<String> roomIds = redisTemplate.opsForZSet()
			.reverseRange(OBSERVER_MAX_ZSET_KEY, 0, -1);
		return buildDebateObserverResponseList(roomIds);
	}

	private List<DebateObserverResponse> buildDebateObserverResponseList(Set<String> roomIds) {
		List<DebateObserverResponse> responseList = new ArrayList<>();
		if (roomIds != null) {
			for (String roomId : roomIds) {
				Debate debate = debateRoomStore.get(roomId);
				if (debate != null) {
					int currentCount = getObserverCount(roomId);
					int maxCount = getMaxObserverCount(roomId);
					DebateObserverResponse response = DebateObserverResponse.builder()
						.debate(debate)
						.currentCount((long) currentCount)
						.maxCount((long) maxCount)
						.build();
					responseList.add(response);
				}
			}
		}
		return responseList;
	}
}
