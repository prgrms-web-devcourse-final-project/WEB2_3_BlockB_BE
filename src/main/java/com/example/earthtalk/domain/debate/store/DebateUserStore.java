package com.example.earthtalk.domain.debate.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * ChatUserStore는 각 채팅방(roomId)별로 찬성(pro) 및 반대(con) 사용자 목록을 관리하는 컴포넌트입니다.
 * <p>
 * 이 클래스는 스레드 안전한 ConcurrentHashMap과 ConcurrentHashMap.newKeySet()을 사용하여
 * 각 채팅방의 사용자 집합을 저장하고, 추가/삭제/조회 기능을 제공합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DebateUserStore {

	private static final String PRO_KEY_PREFIX = "debate:pro:";
	private static final String CON_KEY_PREFIX = "debate:con:";
	private static final String SCORE_ZSET_KEY = "debate:score:";

	private final StringRedisTemplate redisTemplate;

	/**
	 * 주어진 채팅방 ID에 대한 찬성 사용자 집합을 반환합니다.
	 * 만약 해당 채팅방이 없으면, 새로운 집합을 생성하여 반환합니다.
	 *
	 * @param roomId 채팅방 식별자
	 * @return 찬성 사용자 집합
	 */
	public Set<String> getProUsers(String roomId) {
		Set<String> members = redisTemplate.opsForSet().members(PRO_KEY_PREFIX + roomId);
		return (members != null) ? members : Collections.emptySet();
	}

	public void addProUser(String roomId, String userId) {
		validateRoomIdAndUserId(roomId, userId);
		redisTemplate.opsForSet().add(PRO_KEY_PREFIX + roomId, userId);
		updateDebateScore(roomId);
	}


	/**
	 * 주어진 채팅방 ID에 대한 반대 사용자 집합을 반환합니다.
	 * 만약 해당 채팅방이 없으면, 새로운 집합을 생성하여 반환합니다.
	 *
	 * @param roomId 채팅방 식별자
	 * @return 반대 사용자 집합
	 */
	public Set<String> getConUsers(String roomId) {
		Set<String> members = redisTemplate.opsForSet().members(CON_KEY_PREFIX + roomId);
		return (members != null) ? members : Collections.emptySet();
	}

	public void addConUser(String roomId, String userId) {
		validateRoomIdAndUserId(roomId, userId);
		redisTemplate.opsForSet().add(CON_KEY_PREFIX + roomId, userId);
		updateDebateScore(roomId);
	}


	/**
	 * 주어진 채팅방 ID의 찬성 사용자 집합을 제거합니다.
	 *
	 * @param roomId 채팅방 식별자
	 */
	public void removeProUsers(String roomId) {
		redisTemplate.delete(PRO_KEY_PREFIX + roomId);
		updateDebateScore(roomId);
	}

	/**
	 * 주어진 채팅방 ID의 반대 사용자 집합을 제거합니다.
	 *
	 * @param roomId 채팅방 식별자
	 */
	public void removeConUsers(String roomId) {
		redisTemplate.delete(CON_KEY_PREFIX + roomId);
		updateDebateScore(roomId);
	}

	/**
	 * 각 채팅방별 찬성 사용자 수를 집계하여 반환합니다.
	 *
	 * @return 방 ID와 찬성 사용자 수의 매핑 정보
	 */
	public Map<String, Integer> getProUserCounts() {
		Map<String, Integer> counts = new HashMap<>();
		Set<String> keys = redisTemplate.keys(PRO_KEY_PREFIX + "*");
		if (keys != null) {
			for (String key : keys) {
				Long size = redisTemplate.opsForSet().size(key);
				String roomId = key.substring(PRO_KEY_PREFIX.length());
				counts.put(roomId, size != null ? size.intValue() : 0);
			}
		}
		return counts;
	}

	/**
	 * 각 채팅방별 반대 사용자 수를 집계하여 반환합니다.
	 *
	 * @return 방 ID와 반대 사용자 수의 매핑 정보
	 */
	public Map<String, Integer> getConUserCounts() {
		Map<String, Integer> counts = new HashMap<>();
		Set<String> keys = redisTemplate.keys(CON_KEY_PREFIX + "*");
		if (keys != null) {
			for (String key : keys) {
				Long size = redisTemplate.opsForSet().size(key);
				String roomId = key.substring(CON_KEY_PREFIX.length());
				counts.put(roomId, size != null ? size.intValue() : 0);
			}
		}
		return counts;
	}

	private void validateRoomIdAndUserId(String roomId, String userId) {
		if (roomId == null || roomId.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
		}
		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage());
		}
	}

	public void updateDebateScore(String roomId) {
		Long proCount = redisTemplate.opsForSet().size(PRO_KEY_PREFIX + roomId);
		Long conCount = redisTemplate.opsForSet().size(CON_KEY_PREFIX + roomId);
		int pro = proCount != null ? proCount.intValue() : 0;
		int con = conCount != null ? conCount.intValue() : 0;
		double score = pro + con ;
		redisTemplate.opsForZSet().add(SCORE_ZSET_KEY, roomId, score);
	}

	public Set<String> fetchProUsers(String roomId) {
		Set<String> members = redisTemplate.opsForSet().members(PRO_KEY_PREFIX + roomId);
		return (members != null) ? members : Collections.emptySet();
	}

	public Set<String> fetchConUsers(String roomId) {
		Set<String> members = redisTemplate.opsForSet().members(CON_KEY_PREFIX + roomId);
		return (members != null) ? members : Collections.emptySet();
	}

	public List<String> getSortedRoomIdsByScoreDesc() {
		ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
		Set<String> sortedKeys = zSetOps.reverseRange(SCORE_ZSET_KEY, 0, -1);
		List<String> roomKeys = new ArrayList<>();
		if (sortedKeys != null) {
			roomKeys.addAll(sortedKeys);
		}
		return roomKeys;
	}

	public void removeDebateRoom(String roomId) {
		// pro, con 관련 키 삭제
		redisTemplate.delete(PRO_KEY_PREFIX + roomId);
		redisTemplate.delete(CON_KEY_PREFIX + roomId);

		// score ZSet에서 roomId 제거
		redisTemplate.opsForZSet().remove(SCORE_ZSET_KEY, roomId);
	}
}
