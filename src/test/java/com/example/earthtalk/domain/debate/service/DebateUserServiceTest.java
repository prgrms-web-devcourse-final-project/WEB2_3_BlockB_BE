package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class DebateUserServiceTest {

	private SimpMessagingTemplate messagingTemplate;
	private DebateManagementService debateManagementService;
	private ChatRoomService chatRoomService;
	private UserRepository userRepository;
	private DebateUserService debateUserService;

	@BeforeEach
	public void setup() {
		messagingTemplate = mock(SimpMessagingTemplate.class);
		debateManagementService = mock(DebateManagementService.class);
		chatRoomService = mock(ChatRoomService.class);
		userRepository = mock(UserRepository.class);

		debateUserService = new DebateUserService(messagingTemplate, debateManagementService, chatRoomService, userRepository);
	}

	@Test
	public void testAddUser_pro() {
		// ChatRoom 생성: roomId "room123", MemberNumberType.T2(최대 인원 3), 기타 메타데이터 설정
		ChatRoom chatRoom = new ChatRoom("room123", MemberNumberType.T2, "Test Room", "Test Subtitle", TimeType.T5, CategoryType.CO, ContinentType.AF);
		when(chatRoomService.getChatRoom("room123")).thenReturn(chatRoom);

		// "pro" 사용자를 추가
		debateUserService.addUser(chatRoom, "proUser", "pro");

		verify(messagingTemplate, atLeastOnce())
			.convertAndSend(eq("/topic/debate/room123"), (Object) argThat(message -> {
				if (!(message instanceof Map)) return false;
				Map<?, ?> map = (Map<?, ?>) message;
				return "user_joined".equals(map.get("event")) &&
					"proUser".equals(map.get("userName"));
			}));

	}

	@Test
	public void testAddUser_con() {
		// ChatRoom 생성: roomId "room456", MemberNumberType.T2(최대 인원 3)
		ChatRoom chatRoom = new ChatRoom("room456", MemberNumberType.T2, "Test Room 2", "Test Subtitle 2", TimeType.T5, CategoryType.CO, ContinentType.AF);
		when(chatRoomService.getChatRoom("room456")).thenReturn(chatRoom);

		// "con" 사용자를 추가
		debateUserService.addUser(chatRoom, "conUser", "con");

		verify(messagingTemplate, atLeastOnce())
			.convertAndSend(eq("/topic/debate/room456"), (Object) argThat(message -> {
				if (!(message instanceof Map)) return false;
				Map<?, ?> map = (Map<?, ?>) message;
				return "user_joined".equals(map.get("event")) &&
					"conUser".equals(map.get("userName"));
			}));

	}

	@Test
	public void testAddUser_exceedCapacity_pro() {
		// ChatRoom 생성: roomId "room999", MemberNumberType.T2 (최대 3명)
		ChatRoom chatRoom = new ChatRoom("room999", MemberNumberType.T2, "Test Room 999", "Test Subtitle 999", TimeType.T5, CategoryType.CO, ContinentType.AF);
		when(chatRoomService.getChatRoom("room999")).thenReturn(chatRoom);

		// 3명의 pro 사용자를 추가
		debateUserService.addUser(chatRoom, "user1", "pro");
		debateUserService.addUser(chatRoom, "user2", "pro");
		debateUserService.addUser(chatRoom, "user3", "pro");

		// 네 번째 추가 시 ConflictException이 발생해야 함
		assertThrows(ConflictException.class, () -> {
			debateUserService.addUser(chatRoom, "user4", "pro");
		});
	}

	@Test
	public void testRemoveUser() {
		// ChatRoom 생성: roomId "room321", 최대 3명
		ChatRoom chatRoom = new ChatRoom("room321", MemberNumberType.T2, "Test Room 321", "Test Subtitle 321", TimeType.T5, CategoryType.CO, ContinentType.AF);
		when(chatRoomService.getChatRoom("room321")).thenReturn(chatRoom);

		// pro 사용자와 con 사용자 추가
		debateUserService.addUser(chatRoom, "proUser1", "pro");
		debateUserService.addUser(chatRoom, "conUser1", "con");

		// 사용자 수가 각각 1로 설정되었는지 확인
		Map<String, Integer> initialCount = debateUserService.getUserCount("room321");
		assertEquals(1, initialCount.get("pro").intValue());
		assertEquals(1, initialCount.get("con").intValue());

		// pro 사용자 제거
		debateUserService.removeUser("room321", "proUser1");

		// 제거 후의 사용자 수 검증 (pro는 0, con은 1)
		Map<String, Integer> updatedCount = debateUserService.getUserCount("room321");
		assertEquals(0, updatedCount.get("pro").intValue());
		assertEquals(1, updatedCount.get("con").intValue());

		// messagingTemplate을 통한 퇴장 메시지 전송이 이루어졌는지 확인 (호출 횟수 검증)
		verify(messagingTemplate, atLeast(2)).convertAndSend(eq("/topic/debate/room321"), (Object) any());
	}
}
