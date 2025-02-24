package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;

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

public class DebateRoomServiceTest {

	private DebateRoomService debateRoomService;
	private DebateRoomStore debateRoomStore;
	private DebateRepository debateRepository;
	private NewsRepository newsRepository;

	// 간단한 인메모리 구현체
	static class InMemoryDebateRoomStore extends DebateRoomStore {
		private final Map<String, Debate> store = new HashMap<>();

		@Override
		public void put(Debate debate) {
			store.put(debate.getUuid().toString(), debate);
		}

		@Override
		public Debate get(String roomId) {
			return store.get(roomId);
		}

		@Override
		public void remove(String roomId) {
			store.remove(roomId);
		}
	}

	@BeforeEach
	public void setup() {
		debateRoomStore = new InMemoryDebateRoomStore();
		debateRoomService = new DebateRoomService(debateRoomStore, debateRepository, newsRepository);
	}

	@Test
	public void createChatRoom_shouldStoreDebateRoomInCache() {
		// given: 테스트용 CreateDebateRoomRequest 생성
		CreateDebateRoomRequest request = new CreateDebateRoomRequest();
		request.setMemberNumber(MemberNumberType.T2);
		request.setTitle("Test Debate Room");
		request.setDescription("Test Description");
		request.setTime(TimeType.T5);
		request.setCategory(CategoryType.CO);
		request.setContinent(ContinentType.AS);

		// when: 채팅방 생성
		String roomId = debateRoomService.createDebateRoom(request);
		assertNotNull(roomId, "생성된 roomId는 null이면 안 됩니다.");

		// then: 생성된 roomId로 ChatRoom이 저장되었는지 확인
		Debate debate = debateRoomService.getDebateRoom(roomId);
		assertNotNull(debate, "캐시에 저장된 ChatRoom은 null이면 안 됩니다.");
		assertEquals(roomId, debate.getUuid(), "ChatRoom의 roomId가 일치해야 합니다.");
		assertEquals("Test Debate Room", debate.getTitle(), "채팅방 제목이 일치해야 합니다.");
		assertEquals("Test Description", debate.getDescription(), "채팅방 설명이 일치해야 합니다.");
	}

	@Test
	public void removeChatRoom_shouldRemoveDebateRoomFromCache() {
		// given: 채팅방 생성
		CreateDebateRoomRequest request = new CreateDebateRoomRequest();
		request.setMemberNumber(MemberNumberType.T2);
		request.setTitle("Test Debate Room");
		request.setDescription("Test Description");
		request.setTime(TimeType.T5);
		request.setCategory(CategoryType.CO);
		request.setContinent(ContinentType.AF);

		String roomId = debateRoomService.createDebateRoom(request);
		assertNotNull(debateRoomService.getDebateRoom(roomId), "채팅방이 생성되어 캐시에 저장되어 있어야 합니다.");

		// when: 채팅방 제거
		debateRoomService.removeDebateRoom(roomId);

		// then: 해당 roomId로 조회 시 null이 반환되어야 함
		assertNull(debateRoomService.getDebateRoom(roomId), "채팅방이 캐시에서 제거되어야 합니다.");
	}
}

