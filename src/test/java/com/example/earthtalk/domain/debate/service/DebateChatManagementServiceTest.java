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
		String uuid = "room-uuid";
		DebateMessage message = new DebateMessage("chat", "user1", "pro", "Hello", now);
		List<DebateMessage> messages = Collections.singletonList(message);

		when(debateService.getDebateByRoomId(uuid)).thenReturn(mockDebate);
		when(debateUserService.getDebateUserByUserName("user1")).thenReturn(mockDebateParticipants);

		debateChatManagementService.saveChatHistory(uuid, messages);

		ArgumentCaptor<List<DebateChat>> captor = ArgumentCaptor.forClass(List.class);
		verify(debateChatRepository, times(1)).saveAll(captor.capture());
		List<DebateChat> savedChats = captor.getValue();
		assertEquals(1, savedChats.size());
		DebateChat savedChat = savedChats.get(0);
		assertEquals(mockDebate, savedChat.getDebate());
		assertEquals(mockDebateParticipants, savedChat.getDebateParticipants());
		assertEquals(FlagType.PRO, savedChat.getFlagType());
		assertEquals("Hello", savedChat.getContent());
		assertEquals(now, savedChat.getTime());
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
		String uuid = "room-uuid";
		DebateMessage validMessage1 = new DebateMessage("chat", "user1", "pro", "Hello", now);
		DebateMessage validMessage2 = new DebateMessage("chat", "user2", "con", "Hi", now.plusMinutes(1));
		DebateMessage invalidMessage = new DebateMessage("update", "user3", "pro", "Update", now.plusMinutes(2));

		List<DebateMessage> messages = Arrays.asList(validMessage1, validMessage2, invalidMessage);

		when(debateService.getDebateByRoomId(uuid)).thenReturn(mockDebate);
		when(debateUserService.getDebateUserByUserName("user1")).thenReturn(mockDebateParticipants);
		DebateParticipants debateParticipants2 = mock(DebateParticipants.class);
		when(debateUserService.getDebateUserByUserName("user2")).thenReturn(debateParticipants2);

		debateChatManagementService.saveChatHistory(uuid, messages);

		ArgumentCaptor<List<DebateChat>> captor = ArgumentCaptor.forClass(List.class);
		verify(debateChatRepository, times(1)).saveAll(captor.capture());
		List<DebateChat> savedChats = captor.getValue();
		assertEquals(2, savedChats.size());

		List<String> contents = savedChats.stream().map(DebateChat::getContent).collect(Collectors.toList());
		assertTrue(contents.contains("Hello"));
		assertTrue(contents.contains("Hi"));

		for (DebateChat chat : savedChats) {
			if (chat.getContent().equals("Hello")) {
				assertEquals(FlagType.PRO, chat.getFlagType());
			} else if (chat.getContent().equals("Hi")) {
				assertEquals(FlagType.CON, chat.getFlagType());
			}
		}
	}
}
