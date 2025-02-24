package com.example.earthtalk.domain.debate.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DebateChatManagementServiceTest {

	@Mock
	private DebateChatRepository debateChatRepository;

	@Mock
	private DebateUserService debateUserService;

	@Mock
	private DebateService debateService;

	@InjectMocks
	private DebateChatManagementService debateChatManagementService;

	private Debate mockDebate;
	private DebateParticipants mockDebateParticipants;
	private LocalDateTime now;

	@BeforeEach
	public void setUp() {
		mockDebate = mock(Debate.class);
		mockDebateParticipants = mock(DebateParticipants.class);
		now = LocalDateTime.now();
	}

	@Test
	public void testSaveChatHistory_withValidMessage() {
		// 테스트용 고유 방 ID 및 현재 시간 설정
		String uuid = "room-uuid";
		LocalDateTime now = LocalDateTime.now();

		// 테스트용 DebateMessage 생성
		DebateMessage message = new DebateMessage("chat", "user1", "pro", "Hello", now);
		List<DebateMessage> messages = Collections.singletonList(message);

		// 모의 객체 설정: DebateService와 DebateUserService가 적절한 객체를 반환하도록 스텁(stub) 처리
		when(debateService.getDebateByRoomId(uuid)).thenReturn(mockDebate);
		when(debateUserService.getDebateUserByUserName("user1")).thenReturn(mockDebateParticipants);

		// 테스트 대상 메서드 호출
		debateChatManagementService.saveChatHistory(uuid, messages);

		// DebateChatRepository.saveAll()이 한 번 호출되었는지, 그리고 인자로 전달된 DebateChat 리스트를 캡처
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<DebateChat>> captor = ArgumentCaptor.forClass(List.class);
		verify(debateChatRepository, times(1)).saveAll(captor.capture());
		List<DebateChat> savedChats = captor.getValue();

		// 저장된 DebateChat 객체 검증: 개수, 연관 Debate, DebateParticipants, 내용, 시간, 후처리 afterPosition 값 등
		assertEquals(1, savedChats.size(), "저장된 채팅 개수가 1개여야 합니다.");
		DebateChat savedChat = savedChats.get(0);
		assertEquals(mockDebate, savedChat.getDebate(), "Debate 객체가 올바르게 설정되어야 합니다.");
		assertEquals(mockDebateParticipants, savedChat.getDebateParticipants(), "DebateParticipants가 올바르게 설정되어야 합니다.");
		// 테스트에서는 DebateMessage의 "pro" 값에 따라 후처리 값이 PRO로 업데이트된다고 가정합니다.
		assertEquals(FlagType.PRO, savedChat.getDebateParticipants().getAfterPosition(), "afterPosition 값이 'PRO'여야 합니다.");
		assertEquals("Hello", savedChat.getContent(), "메시지 내용이 올바르게 설정되어야 합니다.");
		assertEquals(now, savedChat.getTime(), "메시지 전송 시간이 올바르게 설정되어야 합니다.");
	}


	@Test
	public void testSaveChatHistory_withInvalidEvent() {
		String uuid = "room-uuid";
		DebateMessage message = new DebateMessage("update", "user1", "pro", "Ignored", now);
		List<DebateMessage> messages = Collections.singletonList(message);

		when(debateService.getDebateByRoomId(uuid)).thenReturn(mockDebate);
		// "user1"은 실제로 호출되지 because event != "chat", 따라서 lenient stubbing 사용
		lenient().when(debateUserService.getDebateUserByUserName("user1")).thenReturn(mockDebateParticipants);

		debateChatManagementService.saveChatHistory(uuid, messages);

		ArgumentCaptor<List<DebateChat>> captor = ArgumentCaptor.forClass(List.class);
		verify(debateChatRepository, times(1)).saveAll(captor.capture());
		List<DebateChat> savedChats = captor.getValue();
		// Expecting empty list because the message event is not "chat"
		assertTrue(savedChats.isEmpty());
	}

	@Test
	public void testSaveChatHistory_withNullDebateUser() {
		String uuid = "room-uuid";
		DebateMessage message = new DebateMessage("chat", "user1", "pro", "Hello", now);
		List<DebateMessage> messages = Collections.singletonList(message);

		when(debateService.getDebateByRoomId(uuid)).thenReturn(mockDebate);
		when(debateUserService.getDebateUserByUserName("user1")).thenReturn(null);

		debateChatManagementService.saveChatHistory(uuid, messages);

		ArgumentCaptor<List<DebateChat>> captor = ArgumentCaptor.forClass(List.class);
		verify(debateChatRepository, times(1)).saveAll(captor.capture());
		List<DebateChat> savedChats = captor.getValue();
		// Null DebateParticipants should lead to filtering out the message
		assertTrue(savedChats.isEmpty());
	}

	@Test
	public void testSaveChatHistory_withMultipleMessages() {
		// 테스트용 방 ID와 현재 시간
		String uuid = "room-uuid";
		LocalDateTime now = LocalDateTime.now();

		// validMessage1: 타입 "chat", 사용자 "user1", "pro" 메시지 "Hello"
		DebateMessage validMessage1 = new DebateMessage("chat", "user1", "pro", "Hello", now);
		// validMessage2: 타입 "chat", 사용자 "user2", "con" 메시지 "Hi"
		DebateMessage validMessage2 = new DebateMessage("chat", "user2", "con", "Hi", now.plusMinutes(1));
		// invalidMessage: 타입 "update" - 저장 대상이 아님
		DebateMessage invalidMessage = new DebateMessage("update", "user3", "pro", "Update", now.plusMinutes(2));

		List<DebateMessage> messages = Arrays.asList(validMessage1, validMessage2, invalidMessage);

		// 모의 객체 설정: debateService와 debateUserService가 적절한 객체를 반환하도록 스텁 처리
		when(debateService.getDebateByRoomId(uuid)).thenReturn(mockDebate);
		when(debateUserService.getDebateUserByUserName("user1")).thenReturn(mockDebateParticipants);

		DebateParticipants debateParticipants2 = mock(DebateParticipants.class);
		when(debateUserService.getDebateUserByUserName("user2")).thenReturn(debateParticipants2);

		// 테스트 대상 메서드 호출: 유효한 "chat" 타입 메시지들만 저장되어야 함
		debateChatManagementService.saveChatHistory(uuid, messages);

		// ArgumentCaptor를 사용하여 DebateChatRepository.saveAll() 호출 시 전달된 리스트를 캡처
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<DebateChat>> captor = ArgumentCaptor.forClass(List.class);
		verify(debateChatRepository, times(1)).saveAll(captor.capture());
		List<DebateChat> savedChats = captor.getValue();

		// 유효한 메시지 2건만 저장되어야 함
		assertEquals(2, savedChats.size(), "유효한 메시지 건수가 2건이어야 합니다.");

		// 저장된 메시지 내용 검증
		List<String> contents = savedChats.stream()
			.map(DebateChat::getContent)
			.collect(Collectors.toList());
		assertTrue(contents.contains("Hello"), "메시지 'Hello'가 저장되어야 합니다.");
		assertTrue(contents.contains("Hi"), "메시지 'Hi'가 저장되어야 합니다.");

		// 각 저장된 메시지의 플래그(FlagType) 검증:
		// validMessage1의 경우, 플래그는 "pro"에 대응하여 FlagType.PRO 이어야 하고,
		// validMessage2의 경우, 플래그는 "con"에 대응하여 FlagType.CON 이어야 합니다.
		for (DebateChat chat : savedChats) {
			if ("Hello".equals(chat.getContent())) {
				assertEquals(FlagType.PRO, chat.getDebateParticipants().getPosition(), "내용이 'Hello'인 채팅의 플래그는 PRO여야 합니다.");
			} else if ("Hi".equals(chat.getContent())) {
				assertEquals(FlagType.CON, chat.getDebateParticipants().getPosition(), "내용이 'Hi'인 채팅의 플래그는 CON이어야 합니다.");
			}
		}
	}

}
