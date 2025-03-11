package com.example.earthtalk.domain.debate.component;

import com.example.earthtalk.domain.debate.service.DebateTurnManagementService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.domain.debate.dto.SessionInfo;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.service.DebateChatManagementService;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.domain.debate.service.DebateService;
import com.example.earthtalk.domain.debate.service.DebateUserService;
import com.example.earthtalk.domain.debate.service.ObserverChatManagementService;
import com.example.earthtalk.domain.debate.service.ObserverUserService;
import com.example.earthtalk.domain.debate.store.DebateMessageStore;
import com.example.earthtalk.domain.debate.store.ObserverMessageStore;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.IllegalArgumentException;
import com.example.earthtalk.global.exception.SaveFailedException;

/**
 * WebSocketEventListener는 WebSocket 연결 및 연결 해제 이벤트를 처리하여
 * 세션별 사용자 정보를 관리하고, DebateUserService를 통해 사용자 입장/퇴장 처리를 수행합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

	private final DebateUserService debateUserService;
	private final DebateRoomService debateRoomService;
	private final DebateTurnManagementService debateTurnManagementService;

	private final DebateChatManagementService debateChatManagementService;
	private final ObserverChatManagementService observerChatManagementService;
	private final ObserverUserService observerUserService;

	private final WebSocketIdleSessionMonitor webSocketIdleSessionMonitor;

	// 여러 개의 맵 대신 세션 ID와 관련된 정보를 하나의 객체(SessionInfo)로 관리
	private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

	private final Map<String, String> observerSessionMap = new ConcurrentHashMap<>();

	private final DebateMessageStore debateMessageStore;
	private final ObserverMessageStore observerMessageStore;
	private final DebateRepository debateRepository;
	private final DebateService debateService;

	/**
	 * WebSocket 연결 이벤트를 처리하여 세션 정보를 저장하고, 해당 채팅방에 사용자를 추가합니다.
	 *
	 * @param event SessionConnectedEvent 이벤트 객체
	 */
	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = headerAccessor.getSessionId();
		webSocketIdleSessionMonitor.registerSession(sessionId);
		log.info("새로운 WebSocket 연결 수신: sessionId={}", sessionId);
	}

	@EventListener
	public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		String destination = headerAccessor.getDestination();
		log.info("SUBSCRIBE 프레임 수신 - destination: {}", destination);

		// destination이 null이 아니고 "/room-list"로 시작하지 않는 경우 처리
		if (destination != null && !destination.startsWith("/room-list")) {
			// HandshakeInterceptor에서 저장한 세션 속성에서 값 조회
			Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
			String roomId = sessionAttributes != null ? (String) sessionAttributes.get("roomId") : null;
			String userName = sessionAttributes != null ? (String) sessionAttributes.get("userName") : null;
			String position = sessionAttributes != null ? (String) sessionAttributes.get("position") : null;

			log.info("세션 속성 - roomId: {}, userName: {}, position: {}", roomId, userName, position);

			// Debate 관련 구독 처리
			if (destination.startsWith("/topic/debate/")) {
				if (roomId != null && userName != null && position != null) {
					Debate debate = debateRoomService.getDebateRoom(roomId);
					log.info("Debate room 조회 결과 - debate: {}", debate);
					if (debate == null) {
						log.error("Debate room을 찾을 수 없음 - roomId: {}", roomId);
						throw new IllegalArgumentException(ErrorCode.CHAT_NOT_FOUND);
					}
					SessionInfo sessionInfo = new SessionInfo(roomId, userName, position);
					String sessionId = headerAccessor.getSessionId();
					sessionInfoMap.put(sessionId, sessionInfo);
					log.info("세션 정보 저장 완료 - sessionInfo: {}", sessionInfo);
					try {
						debateUserService.addUser(debate, userName, position);
						log.info("Debate 참여 성공 - roomId: {}, userName: {}, position: {}", roomId, userName, position);
					} catch (Exception e) {
						sessionInfoMap.remove(headerAccessor.getSessionId());
						log.error("Debate 사용자 추가 실패 - roomId: {}, userName: {}. 예외 메시지: {}", roomId, userName, e.getMessage(), e);
						throw new IllegalArgumentException(ErrorCode.CHAT_NOT_FOUND);
					}
				} else {
					log.warn("Debate 참여 필수 속성이 누락됨 - roomId: {}, userName: {}, position: {}", roomId, userName, position);
				}
			}
			// Observer 관련 구독 처리
			else if (destination.startsWith("/topic/observer/")) {
				log.debug("Observer 엔드포인트 처리 시작");
				if (roomId != null && userName != null) {
					String sessionId = headerAccessor.getSessionId();
					observerSessionMap.put(sessionId, roomId);
					log.info("Observer 세션 저장 완료 - sessionId: {}, roomId: {}", sessionId, roomId);
					observerUserService.addUser(roomId, userName);
					log.info("Observer 참여 성공 - roomId: {}, userName: {}", roomId, userName);
				} else {
					log.warn("Observer 참여 필수 속성이 누락됨 - roomId: {}, userName: {}", roomId, userName);
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
		// 로그: WebSocket 연결 종료 이벤트 수신
		log.info("WebSocket 연결 종료 이벤트 수신: sessionId={}", event.getSessionId());

		// 로그: 메시지에서 StompHeaderAccessor 생성
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		log.info("StompHeaderAccessor 생성 완료");

		String sessionId = event.getSessionId();
		log.info("Session ID: {}", sessionId);

		// 로그: 세션 속성에서 사용자 이름(userName) 추출
		String userNameAttr = (String) headerAccessor.getSessionAttributes().get("userName");
		log.info("추출된 userName: {}", userNameAttr);

		// 로그: sessionInfoMap에 현재 세션 정보가 있는지 확인
		if (sessionInfoMap.containsKey(sessionId)) {
			log.info("sessionInfoMap에 sessionId 존재: {}", sessionId);
			SessionInfo sessionInfo = sessionInfoMap.remove(sessionId);
			log.info("SessionInfo 제거 완료: {}", sessionInfo);

			if (sessionInfo != null) {
				String debateRoomId = sessionInfo.getRoomId();
				log.info("Debate Room ID: {}", debateRoomId);

				// 로그: 해당 방의 사용자 수를 계산 (pro와 con 합산)
				int proCount = debateUserService.getUserCount(debateRoomId).get("pro");
				int conCount = debateUserService.getUserCount(debateRoomId).get("con");
				int currentUserCount = proCount + conCount;
				log.info("현재 사용자 수 (pro: {}, con: {}, total: {})", proCount, conCount, currentUserCount);

				if (currentUserCount <= 1) {
					log.info("사용자 수가 1 이하이므로 채팅 기록 저장 및 방 상태 업데이트를 시도합니다.");
					List<DebateMessage> debateMessages = debateMessageStore.removeDebateMessages(debateRoomId);
					log.info("삭제된 Debate 메시지 수: {}", debateMessages != null ? debateMessages.size() : 0);

					List<ObserverMessage> observerMessages = observerMessageStore.removeObserverMessages(debateRoomId);
					log.info("삭제된 Observer 메시지 수: {}", observerMessages != null ? observerMessages.size() : 0);

					if (debateMessages != null && !debateMessages.isEmpty()) {
						try {
							log.info("Debate 채팅 기록 저장 시작");
							debateChatManagementService.saveChatHistory(debateRoomId, debateMessages);
							log.info("Observer 채팅 기록 저장 시작");
							observerChatManagementService.saveChatHistory(debateRoomId, observerMessages);
							log.info("Debate 방 상태 업데이트 시작");
							debateRoomService.updateStatus(debateRoomId);
							log.info("채팅 기록 저장 및 방 상태 업데이트 완료");
						} catch(Exception e) {
							log.error("채팅 기록 저장 실패: {}", e.getMessage());
							throw new SaveFailedException(ErrorCode.SAVE_FAILED);
						}
					}
				}
				log.info("Debate 사용자 제거 시작: {}", sessionInfo.getUserName());
				debateUserService.removeUser(debateRoomId, sessionInfo.getUserName());
				log.info("Debate 사용자 제거 완료");
			}
		}

		// Observer 세션 처리
		if (observerSessionMap.containsKey(sessionId)) {
			log.debug("observerSessionMap에 sessionId 존재: {}", sessionId);
			String observerRoomId = observerSessionMap.remove(sessionId);
			log.debug("Observer Room ID: {}", observerRoomId);
			if (observerRoomId != null && userNameAttr != null) {
				log.debug("Observer 사용자 제거 시작: {}", userNameAttr);
				observerUserService.removeUser(observerRoomId, userNameAttr);
				log.debug("Observer 사용자 제거 완료");
			}
		}

		// 로그: 웹소켓 Idle 세션 모니터에서 세션 등록 해제
		log.debug("웹소켓 Idle 세션 모니터에서 sessionId 등록 해제 시작: {}", sessionId);
		webSocketIdleSessionMonitor.unregisterSession(sessionId);
		log.debug("웹소켓 Idle 세션 모니터 등록 해제 완료");
	}

}
