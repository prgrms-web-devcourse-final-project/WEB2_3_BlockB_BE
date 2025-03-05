package com.example.earthtalk.domain.debate.store;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.dto.DebateRoomRedisDto;
import com.example.earthtalk.domain.news.repository.NewsRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DebateRoomStore {

	private static final String KEY = "debateRoomStore";
	private static final String KEY_ZSET = "debateRoomStoreZSet";

	private final RedisTemplate<String, Object> redisTemplate;
	private final NewsRepository newsRepository; // News 엔티티 조회를 위해 추가

	private HashOperations<String, String, DebateRoomRedisDto> hashOps;
	private ZSetOperations<String, Object> zSetOps;

	@PostConstruct
	public void init() {
		hashOps = redisTemplate.opsForHash();
		zSetOps = redisTemplate.opsForZSet();
	}

	public void put(Debate debate) {
		DebateRoomRedisDto redisDto = DebateRoomRedisDto.fromEntity(debate);

		String debateKey = redisDto.getUuid().toString();
		hashOps.put(KEY, debateKey, redisDto);

		double score = redisDto.getCachedTime().toEpochSecond(ZoneOffset.UTC);

		zSetOps.add(KEY_ZSET, debateKey, score);
	}

	public Debate get(String roomId) {
		DebateRoomRedisDto redisDto = hashOps.get(KEY, roomId);
		return redisDto != null ? redisDto.toEntity(newsRepository) : null;
	}

	public void remove(String roomId) {
		hashOps.delete(KEY, roomId);
		zSetOps.remove(KEY_ZSET, roomId);
	}

	public Map<String, DebateRoomRedisDto> getAll() {
		return hashOps.entries(KEY);
	}

	public List<Debate> getSortByTime() {
		ZSetOperations<String, Object> zetOps = redisTemplate.opsForZSet();
		Set<Object> sortedKeys = zetOps.reverseRange(KEY_ZSET, 0 , -1);
		List<Debate> debates = new ArrayList<>();
		if (sortedKeys != null) {
			for (Object key : sortedKeys) {
				DebateRoomRedisDto redisDto = hashOps.get(KEY, key.toString());
				if (redisDto != null) {
					debates.add(redisDto.toEntity(newsRepository));
				}
			}
		}
		return debates;
	}
}
