package com.example.earthtalk.domain.debate.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.model.DebateRoom;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.domain.debate.service.DebateChatManagementService;
import com.example.earthtalk.domain.debate.service.DebateUserService;
import com.example.earthtalk.domain.debate.store.DebateMessageStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocketEventListener의 WebSocket 연결 및 해제 이벤트를 테스트하는 클래스.
 */
@ExtendWith(MockitoExtension.class)
public class WebSocketEventListenerTest {

	@Mock
	private DebateUserService debateUserService;

	@Mock
	private DebateRoomService debateRoomService;

	@Mock
	private DebateChatManagementService debateChatManagementService;

	// 실제 구현을 사용하는 Spy 객체
	@Spy
	private DebateMessageStore debateMessageStore = new DebateMessageStore();

	@InjectMocks
	private WebSocketEventListener eventListener;

	/**
	 * 연결 이벤트를 시뮬레이션하여 sessionInfoMap에 세션 정보를 등록합니다.
	 * 이때, 주어진 roomId에 대해 chatRoomService.getChatRoom()이 올바른 ChatRoom을 반환하도록 stubbing합니다.
	 *
	 * @param sessionId 세션 ID
	 * @param roomId    채팅방 ID
	 * @param userName  사용자 이름
	 * @param position  사용자 포지션 ("pro" 또는 "con")
	 */
	private void simulateConnection(String sessionId, String roomId, String userName, String position) {
		// roomId에 대해 DebateRoom 객체를 반환하도록 stubbing
		DebateRoom debateRoom = new DebateRoom(roomId, null, "Test Room", "Test Subtitle", null, null, null);
		when(debateRoomService.getDebateRoom(roomId)).thenReturn(debateRoom);

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("roomId", roomId);
		sessionAttributes.put("userName", userName);
		sessionAttributes.put("position", position);

		StompHeaderAccessor connectAccessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
		connectAccessor.setSessionAttributes(sessionAttributes);
		connectAccessor.setSessionId(sessionId);
		Message<byte[]> connectMessage = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8),
			connectAccessor.getMessageHeaders());
		SessionConnectedEvent connectEvent = new SessionConnectedEvent(this, connectMessage);
		eventListener.handleWebSocketConnectListener(connectEvent);
	}

	@BeforeEach
	public void setUp() {
		// 불필요한 전역 stubbing은 제거하고, 각 테스트마다 필요한 stubbing을 개별적으로 설정합니다.
	}

	@Test
	public void testHandleWebSocketConnectListener_Valid() {
		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("roomId", "room123");
		sessionAttributes.put("userName", "testUser");
		sessionAttributes.put("position", "pro");

		// DebateRoom stub 설정
		DebateRoom debateRoom = new DebateRoom("room123", null, "Test Room", "Test Subtitle", null, null, null);
		when(debateRoomService.getDebateRoom("room123")).thenReturn(debateRoom);

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
		accessor.setSessionAttributes(sessionAttributes);
		accessor.setSessionId("session123");
		Message<byte[]> message = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8),
			accessor.getMessageHeaders());
		SessionConnectedEvent connectEvent = new SessionConnectedEvent(this, message);

		eventListener.handleWebSocketConnectListener(connectEvent);

		verify(debateUserService, times(1)).addUser(debateRoom, "testUser", "pro");
	}

	@Test
	public void testHandleWebSocketConnectListener_InvalidRoom() {
		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("roomId", "invalidRoom");
		sessionAttributes.put("userName", "testUser");
		sessionAttributes.put("position", "pro");

		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
		accessor.setSessionAttributes(sessionAttributes);
		accessor.setSessionId("session123");
		Message<byte[]> message = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8),
			accessor.getMessageHeaders());
		SessionConnectedEvent connectEvent = new SessionConnectedEvent(this, message);

		when(debateRoomService.getDebateRoom("invalidRoom")).thenReturn(null);

		assertThrows(RuntimeException.class, () -> eventListener.handleWebSocketConnectListener(connectEvent));
	}

	@Test
	public void testHandleWebSocketDisconnectListener_WithoutMessages() {
		// 먼저 연결 이벤트로 세션 정보 등록 (채팅 메시지 저장소에 메시지 없음)
		simulateConnection("session123", "room123", "testUser", "pro");

		// stubbing: 빈 리스트 반환
		doReturn(Collections.emptyList()).when(debateMessageStore).removeDebateMessages("room123");

		StompHeaderAccessor disconnectAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
		disconnectAccessor.setSessionId("session123");
		Message<byte[]> disconnectMessage = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8),
			disconnectAccessor.getMessageHeaders());
		CloseStatus closeStatus = CloseStatus.NORMAL;
		SessionDisconnectEvent disconnectEvent = new SessionDisconnectEvent(this, disconnectMessage, "session123", closeStatus);

		eventListener.handleWebSocketDisconnectListener(disconnectEvent);

		// saveChatHistory가 호출되지 않아야 함
		verify(debateChatManagementService, times(0)).saveChatHistory(anyString(), anyList());
	}

	@Test
	public void testHandleWebSocketDisconnectListener_WithMessages() {
		// 먼저 연결 이벤트로 세션 정보 등록
		simulateConnection("session123", "room123", "testUser", "pro");

		// stubbing: room123에 대해 mockMessages 반환
		List<DebateMessage> mockMessages = new ArrayList<>();
		mockMessages.add(new DebateMessage("chat", "testUser", "pro", "Hello!", LocalDateTime.now()));
		doReturn(mockMessages).when(debateMessageStore).removeDebateMessages("room123");

		StompHeaderAccessor disconnectAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
		disconnectAccessor.setSessionId("session123");
		Message<byte[]> disconnectMessage = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8),
			disconnectAccessor.getMessageHeaders());
		CloseStatus closeStatus = CloseStatus.NORMAL;
		SessionDisconnectEvent disconnectEvent = new SessionDisconnectEvent(this, disconnectMessage, "session123", closeStatus);

		eventListener.handleWebSocketDisconnectListener(disconnectEvent);

		// verify: debateChatService.saveChatHistory가 "room123"과 mockMessages로 호출되었는지
		verify(debateChatManagementService, times(1)).saveChatHistory(eq("room123"), eq(mockMessages));
	}

	@Test
	public void testChatMessageStore_AddAndRemove() {
		// 실제 ChatMessageStore의 add 및 remove 기능 검증
		String roomId = "room123";
		DebateMessage message = new DebateMessage("chat", "testUser", "pro", "Hello!", LocalDateTime.now());

		debateMessageStore.addDebateMessage(roomId, message);
		List<DebateMessage> messages = debateMessageStore.removeDebateMessages(roomId);

		assertEquals(1, messages.size());
		assertEquals("Hello!", messages.get(0).getMessage());
	}
}
