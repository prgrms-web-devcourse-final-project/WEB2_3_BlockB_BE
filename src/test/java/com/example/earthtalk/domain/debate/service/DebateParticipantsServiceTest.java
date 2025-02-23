package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.model.DebateRoom;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.global.exception.ConflictException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
public class DebateParticipantsServiceTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private DebateRoomService debateRoomService;

	@InjectMocks
	private DebateUserService debateUserService;

	private DebateRoom debateRoom;

	private DebateParticipants createTestDebateUser(Debate debate, User user, FlagType position, FlagType afterPosition) {
		return DebateParticipants.builder()
			.debate(debate)
			.user(user)
			.position(position != null ? position : FlagType.NO_POSITION)
			.afterPosition(afterPosition != null ? afterPosition : FlagType.NO_POSITION)
			.build();
	}

	@BeforeEach
	public void setup() {
		debateRoom = new DebateRoom("room123", MemberNumberType.T2, "Test Room", "Test Subtitle", TimeType.T5, CategoryType.CO, ContinentType.AF);
		when(debateRoomService.getDebateRoom("room123")).thenReturn(debateRoom);
		Debate debate = mock(Debate.class);
		// 올바른 `User` 객체를 반환하도록 설정
		User user = User.builder()
			.email("test@example.com")
			.nickname("testUser")
			.introduction("테스트 유저입니다.")
			.profileUrl("https://example.com/profile.jpg")
			.winNumber(0L)
			.drawNumber(0L)
			.defeatNumber(0L)
			.role(Role.ROLE_MEMBER)  // Role Enum 사용
			.socialType(com.example.earthtalk.domain.user.entity.SocialType.GOOGLE)  // SocialType Enum 사용
			.socialId("123456789")
			.build();

		DebateParticipants debateParticipants = createTestDebateUser(debate, user, FlagType.PRO, null);

		assertNotNull(debateParticipants);
		assertEquals(debate, debateParticipants.getDebate());
		assertEquals(user, debateParticipants.getUser());
		assertEquals(FlagType.PRO, debateParticipants.getPosition());
		assertEquals(FlagType.NO_POSITION, debateParticipants.getAfterPosition()); // 기본값 적용 확인
	}

	@Test
	public void testAddUser_pro() {
		debateUserService.addUser(debateRoom, "proUser", "pro");

		verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/debate/room123"), (Object) argThat(message -> {
			if (!(message instanceof Map)) return false;
			Map<?, ?> map = (Map<?, ?>) message;
			return "user_joined".equals(map.get("event")) &&
				"proUser".equals(map.get("userName"));
		}));
	}

	@Test
	public void testAddUser_con() {
		debateUserService.addUser(debateRoom, "conUser", "con");

		verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/debate/room123"), (Object) argThat(message -> {
			if (!(message instanceof Map)) return false;
			Map<?, ?> map = (Map<?, ?>) message;
			return "user_joined".equals(map.get("event")) &&
				"conUser".equals(map.get("userName"));
		}));
	}

	@Test
	public void testAddUser_exceedCapacity_pro() {
		debateUserService.addUser(debateRoom, "user1", "pro");
		debateUserService.addUser(debateRoom, "user2", "pro");
		debateUserService.addUser(debateRoom, "user3", "pro");

		assertThrows(ConflictException.class, () -> {
			debateUserService.addUser(debateRoom, "user4", "pro");
		});
	}

	@Test
	public void testRemoveUser() {
		debateUserService.addUser(debateRoom, "proUser1", "pro");
		debateUserService.addUser(debateRoom, "conUser1", "con");

		Map<String, Integer> initialCount = debateUserService.getUserCount("room123");
		assertEquals(1, initialCount.get("pro").intValue());
		assertEquals(1, initialCount.get("con").intValue());

		debateUserService.removeUser("room123", "proUser1");

		Map<String, Integer> updatedCount = debateUserService.getUserCount("room123");
		assertEquals(0, updatedCount.get("pro").intValue());
		assertEquals(1, updatedCount.get("con").intValue());

		verify(messagingTemplate, atLeast(2)).convertAndSend(eq("/topic/debate/room123"), (Object) any());
	}

	@Test
	public void testSendUserCountUpdate() {
		debateUserService.addUser(debateRoom, "userA", "pro");
		debateUserService.addUser(debateRoom, "userB", "con");

		verify(messagingTemplate, atLeast(1)).convertAndSend(eq("/topic/debate/room123"), (Object) any());
	}
}
