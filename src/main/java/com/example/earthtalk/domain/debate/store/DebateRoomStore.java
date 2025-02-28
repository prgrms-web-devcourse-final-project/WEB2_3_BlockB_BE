package com.example.earthtalk.domain.debate.store;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * ChatRoomStore는 채팅방 정보를 인메모리 캐시에 저장하고 관리하는 컴포넌트입니다.
 * <p>
 * 이 클래스는 채팅방의 고유 식별자(roomId)를 키(key)로, 해당 채팅방 정보를 담은 {@link Debate} 객체를 값(value)으로 저장합니다.
 * ConcurrentHashMap을 사용하여 여러 스레드에서 안전하게 접근할 수 있습니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DebateRoomStore {

	private static final String KEY = "debateRoomStore";
	private static final String KEY_ZSET = "debateRoomStoreZSet";

	private final RedisTemplate<String, Object> redisTemplate;
	private final DebateRepository debateRepository;
	private final ObserverRoomStore observerRoomStore;
	private final DebateUserStore debateUserStore;
	private HashOperations<String, String, Debate> hashOps;
	private ZSetOperations<String, Object> zSetOps;

	@PostConstruct
	public void init() {
		hashOps = redisTemplate.opsForHash();
		zSetOps = redisTemplate.opsForZSet();
	}

	public void put(Debate debate) {
		String debateKey = debate.getUuid().toString();
		hashOps.put(KEY, debateKey, debate);

		double score = debate.getCreatedAt().toEpochSecond(ZoneOffset.UTC);

		zSetOps.add(KEY_ZSET, debateKey, score);
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 반환합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 * @return 해당 roomId에 해당하는 {@link Debate} 객체, 존재하지 않으면 null
	 */
	public Debate get(String roomId) {
		return hashOps.get(KEY, roomId);
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 캐시에서 제거합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 */
	public void remove(String roomId) {
		hashOps.delete(KEY, roomId);
		zSetOps.remove(KEY_ZSET, roomId);
	}

	private DebateMetaDataResponse buildDebateObserverResponse(String roomId) {
		Debate debate = debateRepository.findByUuid(UUID.fromString(roomId))
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		long currentCount = observerRoomStore.getObserverCount(roomId);
		long maxCount = observerRoomStore.getMaxObserverCount(roomId);
		Set<String> proUsers = debateUserStore.getProUsers(roomId);
		Set<String> conUsers = debateUserStore.getConUsers(roomId);

		return DebateMetaDataResponse.builder()
			.debate(debate)
			.currentCount(currentCount)
			.maxCount(maxCount)
			.proUsers(proUsers)
			.conUsers(conUsers)
			.build();
	}

	public List<DebateMetaDataResponse> getAllSortedByCreatedAt()
	{
		Set<Object> debateKeys = zSetOps.reverseRange(KEY_ZSET, 0, -1);
		List<DebateMetaDataResponse> responseList = new ArrayList<>();
		if (debateKeys != null) {
			for (Object debateKey : debateKeys) {
				DebateMetaDataResponse response = buildDebateObserverResponse(String.valueOf(debateKey));
				if (response != null) {
					responseList.add(response);
				}
			}
		}
		return responseList;
	}
	/**
	 * 현재 캐시에 저장된 모든 채팅방 정보를 반환합니다.
	 *
	 * @return 모든 채팅방 정보를 담은 Map
	 */
	public Map<String, Debate> getAll() {
		return hashOps.entries(KEY);
	}
}
