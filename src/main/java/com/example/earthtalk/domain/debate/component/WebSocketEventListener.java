package com.example.earthtalk.domain.debate.component;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.example.earthtalk.domain.debate.dto.SessionInfo;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.debate.service.ChatRoomService;
import com.example.earthtalk.domain.debate.service.DebateUserService;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;

/**
 * WebSocketEventListener는 WebSocket 연결 및 연결 해제 이벤트를 처리하여
 * 세션별 사용자 정보를 관리하고, DebateUserService를 통해 사용자 입장/퇴장 처리를 수행합니다.
 */
@Component
public class WebSocketEventListener {

	private final DebateUserService debateUserService;
	private final ChatRoomService chatRoomService;

	// 여러 개의 맵 대신 세션 ID와 관련된 정보를 하나의 객체(SessionInfo)로 관리
	private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();


	public WebSocketEventListener(DebateUserService debateUserService, ChatRoomService chatRoomService) {
		this.debateUserService = debateUserService;
		this.chatRoomService = chatRoomService;
	}

	/**
	 * WebSocket 연결 이벤트를 처리하여 세션 정보를 저장하고, 해당 채팅방에 사용자를 추가합니다.
	 *
	 * @param event SessionConnectedEvent 이벤트 객체
	 */
	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();
		String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
		String userName = (String) headerAccessor.getSessionAttributes().get("userName");
		String position = (String) headerAccessor.getSessionAttributes().get("position");

		if (roomId != null && userName != null && position != null) {
			SessionInfo sessionInfo = new SessionInfo(roomId, userName, position);
			sessionInfoMap.put(sessionId, sessionInfo);

			ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
			if (chatRoom == null) {
				throw new BadRequestException(ErrorCode.BAD_REQUEST);
			}
			debateUserService.addUser(chatRoom, userName, position);
		}
	}

	/**
	 * WebSocket 연결 해제 이벤트를 처리하여 세션 정보를 제거하고, 해당 사용자를 Debate 방에서 제거합니다.
	 *
	 * @param event SessionDisconnectEvent 이벤트 객체
	 */
	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		String sessionId = event.getSessionId();
		SessionInfo sessionInfo = sessionInfoMap.remove(sessionId);
		if (sessionInfo != null) {
			debateUserService.removeUser(sessionInfo.getRoomId(), sessionInfo.getUserName());
		}
	}
}
