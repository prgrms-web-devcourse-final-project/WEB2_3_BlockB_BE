package com.example.earthtalk.domain.debate.component;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.domain.debate.dto.SessionInfo;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.service.DebateChatManagementService;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.domain.debate.service.DebateUserService;
import com.example.earthtalk.domain.debate.service.ObserverChatManagementService;
import com.example.earthtalk.domain.debate.service.ObserverUserService;
import com.example.earthtalk.domain.debate.store.DebateMessageStore;
import com.example.earthtalk.domain.debate.store.ObserverMessageStore;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.IllegalArgumentException;

/**
 * WebSocketEventListener는 WebSocket 연결 및 연결 해제 이벤트를 처리하여
 * 세션별 사용자 정보를 관리하고, DebateUserService를 통해 사용자 입장/퇴장 처리를 수행합니다.
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final DebateUserService debateUserService;
	private final DebateRoomService debateRoomService;

	private final DebateChatManagementService debateChatManagementService;
	private final ObserverChatManagementService observerChatManagementService;
	private final ObserverUserService observerUserService;

	// 여러 개의 맵 대신 세션 ID와 관련된 정보를 하나의 객체(SessionInfo)로 관리
	private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

	private final Map<String, String> observerSessionMap = new ConcurrentHashMap<>();

	private final DebateMessageStore debateMessageStore;
	private final ObserverMessageStore observerMessageStore;

	/**
	 * WebSocket 연결 이벤트를 처리하여 세션 정보를 저장하고, 해당 채팅방에 사용자를 추가합니다.
	 *
	 * @param event SessionConnectedEvent 이벤트 객체
	 */
	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = headerAccessor.getDestination();
		if (destination != null && !destination.startsWith("/room-list")) {
			String roomId = (String)headerAccessor.getSessionAttributes().get("roomId");
			String userName = (String)headerAccessor.getSessionAttributes().get("userName");
			if (destination.startsWith("/topic/debate/")) {
				String sessionId = headerAccessor.getSessionId();
				String position = (String)headerAccessor.getSessionAttributes().get("position");
				if (roomId != null && userName != null && position != null) {
					SessionInfo sessionInfo = new SessionInfo(roomId, userName, position);
					sessionInfoMap.put(sessionId, sessionInfo);

					Debate debate = debateRoomService.getDebateRoom(roomId);
					if (debate == null) {
						throw new IllegalArgumentException(ErrorCode.CHAT_NOT_FOUND);
					}
					debateUserService.addUser(debate, userName, position);
				}
			} else if (destination.startsWith("/topic/observer/")) {
				String sessionId = headerAccessor.getSessionId();
				if (roomId != null && userName != null) {
					observerSessionMap.put(sessionId, roomId);
					observerUserService.addUser(roomId, userName);
				}
			}

		}
	}

	/**
	 * WebSocket 연결 해제 이벤트를 처리합니다.
	 * <p>
	 * 사용자가 WebSocket에서 연결을 해제하면, 해당 세션 정보를 제거하고 사용자를 채팅방에서 삭제합니다.
	 * 또한, 사용자가 속해 있던 채팅방의 채팅 메시지를 저장소에서 제거한 후, 영속 저장소(DB)에 저장합니다.
	 * </p>
	 *
	 * <h3>처리 과정:</h3>
	 * <ol>
	 *     <li>세션 정보를 조회하여 해당 사용자의 roomId를 가져옵니다.</li>
	 *     <li>채팅 메시지 저장소({@link DebateMessageStore})에서 해당 roomId의 메시지를 가져옵니다.</li>
	 *     <li>메시지가 존재하면 {@link DebateChatManagementService#saveChatHistory(String, List)}를 호출하여 DB에 저장합니다.</li>
	 *     <li>사용자를 {@link DebateUserService#removeUser(String, String)}를 통해 채팅방에서 제거합니다.</li>
	 * </ol>
	 *
	 * @param event {@link SessionDisconnectEvent} - WebSocket 연결 해제 이벤트 객체
	 */
	@EventListener
	public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = event.getSessionId();
		String userNameAttr = (String)headerAccessor.getSessionAttributes().get("userName");
		if (sessionInfoMap.containsKey(sessionId)) {
			SessionInfo sessionInfo = sessionInfoMap.remove(sessionId);
			if (sessionInfo != null) {
				String debateRoomId = sessionInfo.getRoomId();

				int currentUserCount = debateUserService.getUserCount(debateRoomId).get("pro") +
					debateUserService.getUserCount(debateRoomId).get("con");

				if (currentUserCount <= 1) {
					List<DebateMessage> debateMessages = debateMessageStore.removeDebateMessages(debateRoomId);

					List<ObserverMessage> observerMessages = observerMessageStore.removeObserverMessages(debateRoomId);
					if (debateMessages != null && !debateMessages.isEmpty()) {
						debateChatManagementService.saveChatHistory(debateRoomId, debateMessages);
						observerChatManagementService.saveChatHistory(debateRoomId, observerMessages);
					}
				}
				debateUserService.removeUser(debateRoomId, sessionInfo.getUserName());
			}
		}

		if (observerSessionMap.containsKey(sessionId)) {
			String observerRoomId = observerSessionMap.remove(sessionId);
			if (observerRoomId != null && userNameAttr != null) {
				observerUserService.removeUser(observerRoomId, userNameAttr);
			}
		}

	}
}
