package com.example.earthtalk.domain.debate.component;

import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.example.earthtalk.domain.debate.dto.SessionInfo;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.debate.service.ChatRoomService;
import com.example.earthtalk.domain.debate.service.DebateUserService;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

public class WebSocketEventListenerTest {

	private DebateUserService debateUserService;
	private ChatRoomService chatRoomService;
	private WebSocketEventListener eventListener;

	@BeforeEach
	public void setUp() {
		debateUserService = mock(DebateUserService.class);
		chatRoomService = mock(ChatRoomService.class);
		eventListener = new WebSocketEventListener(debateUserService, chatRoomService);
	}

	@Test
	public void testHandleWebSocketConnectListener_Valid() {
		// 세션 속성 맵 생성
		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("roomId", "room123");
		sessionAttributes.put("userName", "testUser");
		sessionAttributes.put("position", "pro");

		// StompHeaderAccessor 생성 (CONNECTED 커맨드를 사용)
		StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
		accessor.setSessionAttributes(sessionAttributes);
		accessor.setSessionId("session123");
		Message<byte[]> message = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());

		// CONNECTED 이벤트 생성 (source로 this를 전달)
		SessionConnectedEvent connectEvent = new SessionConnectedEvent(this, message);

		// chatRoomService가 "room123"에 대해 ChatRoom 객체를 반환하도록 stub 설정
		ChatRoom chatRoom = new ChatRoom("room123", /*MemberNumberType*/ null, "Test Room", "Test Subtitle", null, null, null);
		// 실제 테스트에서는 MemberNumberType, TimeType, CategoryType, ContinentType을 적절히 설정해야 합니다.
		when(chatRoomService.getChatRoom("room123")).thenReturn(chatRoom);

		// 이벤트 처리
		eventListener.handleWebSocketConnectListener(connectEvent);

		// DebateUserService.addUser가 올바른 파라미터로 호출되었는지 검증
		verify(debateUserService, times(1)).addUser(chatRoom, "testUser", "pro");
	}

	@Test
	public void testHandleWebSocketDisconnectListener_Valid() {
		// 먼저 연결 이벤트를 발생시켜 sessionInfoMap에 세션 정보를 저장합니다.
		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("roomId", "room123");
		sessionAttributes.put("userName", "testUser");
		sessionAttributes.put("position", "pro");

		StompHeaderAccessor connectAccessor = StompHeaderAccessor.create(StompCommand.CONNECTED);
		connectAccessor.setSessionAttributes(sessionAttributes);
		connectAccessor.setSessionId("session123");
		Message<byte[]> connectMessage = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8), connectAccessor.getMessageHeaders());
		SessionConnectedEvent connectEvent = new SessionConnectedEvent(this, connectMessage);
		ChatRoom chatRoom = new ChatRoom("room123", /*MemberNumberType*/ null, "Test Room", "Test Subtitle", null, null, null);
		when(chatRoomService.getChatRoom("room123")).thenReturn(chatRoom);
		eventListener.handleWebSocketConnectListener(connectEvent);

		// 이제 연결 해제 이벤트를 생성합니다.
		StompHeaderAccessor disconnectAccessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
		disconnectAccessor.setSessionId("session123");
		// 연결 해제 이벤트의 메시지에는 세션 속성이 필요없으므로 단순하게 payload 생성
		Message<byte[]> disconnectMessage = MessageBuilder.createMessage("dummy".getBytes(StandardCharsets.UTF_8), disconnectAccessor.getMessageHeaders());
		// 최신 버전에서는 SessionDisconnectEvent 생성자가 source, message, sessionId, closeStatus를 요구합니다.
		// closeStatus는 정상 종료를 의미하는 CloseStatus.NORMAL을 사용합니다.
		org.springframework.web.socket.CloseStatus closeStatus = org.springframework.web.socket.CloseStatus.NORMAL;
		SessionDisconnectEvent disconnectEvent = new SessionDisconnectEvent(this, disconnectMessage, "session123", closeStatus);

		// 이벤트 처리
		eventListener.handleWebSocketDisconnectListener(disconnectEvent);

		// DebateUserService.removeUser가 올바른 파라미터로 호출되었는지 검증
		verify(debateUserService, times(1)).removeUser("room123", "testUser");
	}
}
