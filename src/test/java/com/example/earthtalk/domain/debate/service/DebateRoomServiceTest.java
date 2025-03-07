package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.earthtalk.domain.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.constant.ContinentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.redis.core.RedisTemplate;

public class DebateRoomServiceTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	private DebateRoomService debateRoomService;
	private DebateRoomStore debateRoomStore;
	private DebateRepository debateRepository;
	private NewsRepository newsRepository;
	private UserRepository userRepository;
	//
	// // 간단한 인메모리 구현체
	// static class InMemoryDebateRoomStore extends DebateRoomStore {
	// 	private final Map<String, Debate> store = new HashMap<>();
	//
	// 	public InMemoryDebateRoomStore(RedisTemplate<String, Object> redisTemplate) {
	// 		super(redisTemplate);
	// 	}
	//
	// 	@Override
	// 	public void put(Debate debate) {
	// 		store.put(debate.getUuid().toString(), debate);
	// 	}
	//
	// 	@Override
	// 	public Debate get(String roomId) {
	// 		return store.get(roomId);
	// 	}
	//
	// 	@Override
	// 	public void remove(String roomId) {
	// 		store.remove(roomId);
	// 	}
	// }




}

