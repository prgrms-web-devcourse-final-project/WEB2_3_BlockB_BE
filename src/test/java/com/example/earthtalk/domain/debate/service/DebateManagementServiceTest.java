package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateUser;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.repository.DebateUserRepository;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class DebateManagementServiceTest {

	private DebateUserRepository debateUserRepository;
	private DebateChatRepository debateChatRepository;
	private DebateRepository debateRepository;
	private UserRepository userRepository;
	private ChatRoomService chatRoomService;
	private DebateManagementService debateManagementService;

	@BeforeEach
	public void setUp() {
		debateUserRepository = Mockito.mock(DebateUserRepository.class);
		debateChatRepository = Mockito.mock(DebateChatRepository.class);
		debateRepository = Mockito.mock(DebateRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		chatRoomService = Mockito.mock(ChatRoomService.class);

		debateManagementService = new DebateManagementService(
			debateUserRepository,
			debateChatRepository,
			debateRepository,
			userRepository,
			chatRoomService
		);
	}

	@Test
	public void persistChatRoomIfFull_whenRoomIsFull_shouldPersistDebateAndUsers() {
		// given
		String roomId = "room1";

		// ChatRoom 목 객체 생성 및 필요한 메서드 stub 설정
		ChatRoom chatRoom = Mockito.mock(ChatRoom.class);
		when(chatRoom.getTitle()).thenReturn("Test Room");
		when(chatRoom.getSubtitle()).thenReturn("Test Subtitle");
		when(chatRoom.getMemberNumberType()).thenReturn(MemberNumberType.T2);
		when(chatRoom.getContinent()).thenReturn(ContinentType.AS);
		when(chatRoom.getCategory()).thenReturn(CategoryType.CO);
		when(chatRoom.getTime()).thenReturn(TimeType.T5);
		when(chatRoom.isFull()).thenReturn(true);
		when(chatRoomService.getChatRoom(roomId)).thenReturn(chatRoom);

		// pro와 con 사용자 이름 셋 구성
		Set<String> proUserNames = Set.of("proUser");
		Set<String> conUserNames = Set.of("conUser");

		// UserRepository에서 닉네임으로 User를 조회하도록 stub 설정
		User proUser = Mockito.mock(User.class);
		User conUser = Mockito.mock(User.class);
		when(userRepository.findByNickname("proUser")).thenReturn(java.util.Optional.of(proUser));
		when(userRepository.findByNickname("conUser")).thenReturn(java.util.Optional.of(conUser));

		// when
		debateManagementService.persistChatRoomIfFull(roomId, proUserNames, conUserNames);

		// then
		// Debate 엔티티 저장 검증
		ArgumentCaptor<Debate> debateCaptor = ArgumentCaptor.forClass(Debate.class);
		verify(debateRepository, times(1)).save(debateCaptor.capture());
		Debate savedDebate = debateCaptor.getValue();
		assertEquals("Test Room", savedDebate.getTitle());
		// Debate 엔티티에서는 description 필드에 채팅방의 부제(Subtitle)를 사용했다고 가정
		assertEquals("Test Subtitle", savedDebate.getDescription());
		assertEquals(MemberNumberType.T2, savedDebate.getMember());
		assertEquals("아시아", savedDebate.getContinent().getValue());
		assertEquals("칼럼", savedDebate.getCategory().getValue());
		assertEquals(5, savedDebate.getTime().getValue());
		assertEquals(RoomType.DEBATE, savedDebate.getStatus());
		assertEquals(0L, savedDebate.getAgreeNumber());
		assertEquals(0L, savedDebate.getDisagreeNumber());

		// DebateUser 저장 검증 (각각 pro, con 사용자에 대해)
		ArgumentCaptor<DebateUser> debateUserCaptor = ArgumentCaptor.forClass(DebateUser.class);
		verify(debateUserRepository, times(2)).save(debateUserCaptor.capture());
		List<DebateUser> debateUsers = debateUserCaptor.getAllValues();
		boolean foundPro = debateUsers.stream().anyMatch(du -> du.getPosition() == FlagType.PRO);
		boolean foundCon = debateUsers.stream().anyMatch(du -> du.getPosition() == FlagType.CON);
		assertTrue(foundPro, "Pro user should be persisted");
		assertTrue(foundCon, "Con user should be persisted");

		// 채팅방 캐시에서 제거되었는지 검증
		verify(chatRoomService, times(1)).removeChatRoom(roomId);
	}

	@Test
	public void persistChatRoomIfFull_whenRoomNotFull_shouldOnlyRemoveChatRoom() {
		// given
		String roomId = "room2";
		ChatRoom chatRoom = Mockito.mock(ChatRoom.class);
		when(chatRoom.isFull()).thenReturn(false);
		when(chatRoomService.getChatRoom(roomId)).thenReturn(chatRoom);

		// when
		debateManagementService.persistChatRoomIfFull(roomId, Set.of("proUser"), Set.of("conUser"));

		// then: Room이 꽉 차지 않은 경우 Debate 및 DebateUser 저장은 이루어지지 않아야 함
		verify(debateRepository, never()).save(Mockito.any());
		verify(debateUserRepository, never()).save(Mockito.any());
		// 제거는 항상 수행
		verify(chatRoomService, times(1)).removeChatRoom(roomId);
	}
}
