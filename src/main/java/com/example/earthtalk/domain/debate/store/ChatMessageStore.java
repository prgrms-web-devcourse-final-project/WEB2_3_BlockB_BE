package com.example.earthtalk.domain.debate.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.dto.DebateMessage;

/**
 * ChatMessageStore는 채팅방(roomId)별로 DebateMessage를 인메모리로 저장하고 관리하는 컴포넌트입니다.
 * <p>
 * 이 클래스는 채팅 메시지를 추가, 조회, 삭제하는 메서드를 제공하며,
 * 여러 스레드에서 안전하게 접근할 수 있도록 ConcurrentHashMap을 사용합니다.
 * </p>
 */
@Component
public class ChatMessageStore {

	/**
	 * 채팅방 ID를 키(key)로, 해당 채팅방의 DebateMessage 리스트를 값(value)으로 갖는 맵.
	 */
	private final Map<String, List<DebateMessage>> chatMessagesMap = new ConcurrentHashMap<>();

	/**
	 * 지정된 roomId에 해당하는 DebateMessage 리스트를 반환합니다.
	 * <p>
	 * 만약 해당 roomId가 존재하지 않는다면, 새로운 ArrayList를 생성하여 맵에 추가한 후 반환합니다.
	 * </p>
	 *
	 * @param roomId 채팅방의 식별자
	 * @return 해당 채팅방의 DebateMessage 리스트
	 */
	public List<DebateMessage> getOrCreateChatMessages(String roomId) {
		return chatMessagesMap.computeIfAbsent(roomId, k -> new ArrayList<>());
	}

	/**
	 * 지정된 roomId에 DebateMessage를 추가합니다.
	 *
	 * @param roomId  채팅방의 식별자
	 * @param message 추가할 DebateMessage 객체
	 */
	public void addChatMessage(String roomId, DebateMessage message) {
		getOrCreateChatMessages(roomId).add(message);
	}

	/**
	 * 지정된 roomId에 해당하는 모든 DebateMessage 리스트를 제거하고 반환합니다.
	 * <p>
	 * 만약 해당 roomId가 존재하지 않으면, null을 반환합니다.
	 * </p>
	 *
	 * @param roomId 채팅방의 식별자
	 * @return 제거된 DebateMessage 리스트, 존재하지 않으면 null
	 */
	public List<DebateMessage> removeChatMessages(String roomId) {
		return chatMessagesMap.remove(roomId);
	}

	/**
	 * 저장된 모든 채팅 메시지 맵을 반환합니다.
	 * <p>
	 * 이 메서드는 디버깅이나 관리 용도로 사용될 수 있습니다.
	 * </p>
	 *
	 * @return 채팅방 ID와 DebateMessage 리스트의 맵
	 */
	public Map<String, List<DebateMessage>> getAllChatMessages() {
		return chatMessagesMap;
	}
}
