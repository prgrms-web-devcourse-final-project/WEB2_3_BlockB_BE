package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
//import com.example.earthtalk.domain.debate.repository.DebateUserRepository;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.constant.ContinentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ChatRoomServiceTest {

	private ChatRoomService chatRoomService;
	private DebateRepository debateRepository;
	//private DebateUserRepository debateUserRepository;
	private UserRepository userRepository;

	@BeforeEach
	public void setup() {
		// 목 객체 생성
		debateRepository = Mockito.mock(DebateRepository.class);
		//debateUserRepository = Mockito.mock(DebateUserRepository.class);
		userRepository = Mockito.mock(UserRepository.class);

		//chatRoomService = new ChatRoomService(debateRepository, debateUserRepository, userRepository);
	}

	@Test
	public void createChatRoom_shouldStoreChatRoomInCache() {
		// given: 테스트용 CreateDebateRoomRequest 생성 및 설정
		CreateDebateRoomRequest request = new CreateDebateRoomRequest();
		// MemberNumberType는 예시로 T2 (예: 3명을 의미)라고 가정
		request.setMemberNumber(MemberNumberType.T2);
		request.setTitle("Test Debate Room");
		request.setDescription("Test Description");
		// time, category, continent는 String 또는 해당 Enum 값이라고 가정합니다.
		request.setTime(TimeType.T5);
		request.setCategory(CategoryType.CO);
		request.setContinent(ContinentType.AS);

		// when: 채팅방 생성
		String roomId = chatRoomService.createChatRoom(request);
		assertNotNull(roomId, "생성된 roomId는 null이면 안 됩니다.");

		// then: 캐시에서 해당 roomId로 ChatRoom 객체를 조회
		ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
		assertNotNull(chatRoom, "캐시에 저장된 ChatRoom은 null이면 안 됩니다.");
		assertEquals(roomId, chatRoom.getRoomId(), "ChatRoom의 roomId가 일치해야 합니다.");
		assertEquals("Test Debate Room", chatRoom.getTitle(), "채팅방 제목이 일치해야 합니다.");
		// ChatRoom 생성자에서 description을 subtitle으로 저장한 경우
		assertEquals("Test Description", chatRoom.getSubtitle(), "채팅방 설명이 일치해야 합니다.");
		// 나머지 필드에 대해서도 동일하게 검증할 수 있습니다.
	}

	@Test
	public void removeChatRoom_shouldRemoveChatRoomFromCache() {
		// given: 채팅방 생성
		CreateDebateRoomRequest request = new CreateDebateRoomRequest();
		request.setMemberNumber(MemberNumberType.T2);
		request.setTitle("Test Debate Room");
		request.setDescription("Test Description");
		request.setTime(TimeType.T5);
		request.setCategory(CategoryType.CO);
		request.setContinent(ContinentType.AF);

		String roomId = chatRoomService.createChatRoom(request);
		assertNotNull(chatRoomService.getChatRoom(roomId), "채팅방이 생성되어 캐시에 저장되어 있어야 합니다.");

		// when: 채팅방 제거
		chatRoomService.removeChatRoom(roomId);

		// then: 캐시에서 해당 roomId 조회 시 null이어야 함
		assertNull(chatRoomService.getChatRoom(roomId), "채팅방이 캐시에서 제거되어야 합니다.");
	}
}
