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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
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
		log.info("put 메서드 호출 - Debate 생성 시작, Debate ID: {}", debate.getId());

		DebateRoomRedisDto redisDto = DebateRoomRedisDto.fromEntity(debate);
		log.debug("DebateRoomRedisDto 변환 완료: {}", redisDto);

		String debateKey = redisDto.getUuid().toString();
		log.debug("생성된 debateKey: {}", debateKey);

		hashOps.put(KEY, debateKey, redisDto);
		log.debug("Redis hashOps에 put 완료 - KEY: {}, debateKey: {}", KEY, debateKey);

		double score = redisDto.getCachedTime().toEpochSecond(ZoneOffset.UTC);
		log.debug("계산된 score: {}", score);

		zSetOps.add(KEY_ZSET, debateKey, score);
		log.debug("Redis zSetOps에 add 완료 - KEY_ZSET: {}, debateKey: {}, score: {}", KEY_ZSET, debateKey, score);
	}

	public Debate get(String roomId) {
		log.info("get 메서드 호출 - roomId: {}", roomId);

		DebateRoomRedisDto redisDto = hashOps.get(KEY, roomId);
		if (redisDto != null) {
			log.debug("Redis에서 조회된 DebateRoomRedisDto: {}", redisDto);
			Debate debate = redisDto.toEntity(newsRepository);
			log.debug("변환된 Debate 엔티티: {}", debate);
			return debate;
		} else {
			log.warn("Redis에서 DebateRoomRedisDto를 조회하지 못함 - roomId: {}", roomId);
			return null;
		}
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
