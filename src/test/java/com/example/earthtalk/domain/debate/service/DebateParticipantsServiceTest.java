package com.example.earthtalk.domain.debate.service;

import static com.example.earthtalk.domain.debate.entity.QDebate.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.user.entity.Role;
import com.example.earthtalk.domain.user.entity.SocialType;
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

	private Debate debate;

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
		// Debate 객체를 Builder나 생성자를 통해 생성합니다.
		Debate debate = Debate.builder()
			.uuid(UUID.fromString("00000000-0000-0000-0000-000000000123"))
			.member(MemberNumberType.T2)
			.title("Test Room")
			.description("Test Subtitle")
			.time(TimeType.T5)
			.category(CategoryType.CO)
			.continent(ContinentType.AF)
			.build();

		// debateRoomService가 "room123"에 대해 위 debate 객체를 반환하도록 설정합니다.
		when(debateRoomService.getDebateRoom("room123")).thenReturn(debate);

		// 올바른 User 객체 생성
		User user = User.builder()
			.email("test@example.com")
			.nickname("testUser")
			.introduction("테스트 유저입니다.")
			.profileUrl("https://example.com/profile.jpg")
			.winNumber(0L)
			.drawNumber(0L)
			.defeatNumber(0L)
			.role(Role.ROLE_MEMBER)
			.socialType(SocialType.GOOGLE)
			.socialId("123456789")
			.build();

		// DebateParticipants를 생성하는 헬퍼 메서드를 호출합니다.
		DebateParticipants debateParticipants = createTestDebateUser(debate, user, FlagType.PRO, null);

		// 생성된 DebateParticipants의 각 필드가 올바르게 설정되었는지 검증합니다.
		assertNotNull(debateParticipants);
		assertEquals(debate, debateParticipants.getDebate());
		assertEquals(user, debateParticipants.getUser());
		assertEquals(FlagType.PRO, debateParticipants.getPosition());
		// 후처리 기본값: afterPosition이 설정되지 않았다면, @PrePersist에서 NO_POSITION으로 처리됨
		assertEquals(FlagType.NO_POSITION, debateParticipants.getAfterPosition());
	}


	@Test
	public void testAddUser_pro() {
		debateUserService.addUser(debate, "proUser", "pro");

		verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/debate/room123"), (Object) argThat(message -> {
			if (!(message instanceof Map)) return false;
			Map<?, ?> map = (Map<?, ?>) message;
			return "user_joined".equals(map.get("event")) &&
				"proUser".equals(map.get("userName"));
		}));
	}

	@Test
	public void testAddUser_con() {
		debateUserService.addUser(debate, "conUser", "con");

		verify(messagingTemplate, atLeastOnce()).convertAndSend(eq("/topic/debate/room123"), (Object) argThat(message -> {
			if (!(message instanceof Map)) return false;
			Map<?, ?> map = (Map<?, ?>) message;
			return "user_joined".equals(map.get("event")) &&
				"conUser".equals(map.get("userName"));
		}));
	}

	@Test
	public void testAddUser_exceedCapacity_pro() {
		debateUserService.addUser(debate, "user1", "pro");
		debateUserService.addUser(debate, "user2", "pro");
		debateUserService.addUser(debate, "user3", "pro");

		assertThrows(ConflictException.class, () -> {
			debateUserService.addUser(debate, "user4", "pro");
		});
	}

	@Test
	public void testRemoveUser() {
		debateUserService.addUser(debate, "proUser1", "pro");
		debateUserService.addUser(debate, "conUser1", "con");

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
		debateUserService.addUser(debate, "userA", "pro");
		debateUserService.addUser(debate, "userB", "con");

		verify(messagingTemplate, atLeast(1)).convertAndSend(eq("/topic/debate/room123"), (Object) any());
	}
}
