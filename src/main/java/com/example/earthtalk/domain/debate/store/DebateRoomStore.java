package com.example.earthtalk.domain.debate.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.earthtalk.domain.debate.entity.Debate;

/**
 * ChatRoomStore는 채팅방 정보를 인메모리 캐시에 저장하고 관리하는 컴포넌트입니다.
 * <p>
 * 이 클래스는 채팅방의 고유 식별자(roomId)를 키(key)로, 해당 채팅방 정보를 담은 {@link Debate} 객체를 값(value)으로 저장합니다.
 * ConcurrentHashMap을 사용하여 여러 스레드에서 안전하게 접근할 수 있습니다.
 * </p>
 */
@Component
public class DebateRoomStore {

	private final Map<String, Debate> debateRoomCache = new ConcurrentHashMap<>();

	/**
	 * 주어진 채팅방 정보를 캐시에 저장합니다.
	 *
	 * @param debate 저장할 {@link Debate} 객체
	 */
	public void put(Debate debate) {
		debateRoomCache.put(String.valueOf(debate.getUuid()), debate);
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 반환합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 * @return 해당 roomId에 해당하는 {@link Debate} 객체, 존재하지 않으면 null
	 */
	public Debate get(String roomId) {
		return debateRoomCache.get(roomId);
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 캐시에서 제거합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 */
	public void remove(String roomId) {
		debateRoomCache.remove(roomId);
	}

	/**
	 * 현재 캐시에 저장된 모든 채팅방 정보를 반환합니다.
	 *
	 * @return 모든 채팅방 정보를 담은 Map
	 */
	public Map<String, Debate> getAll() {
		return debateRoomCache;
	}
}
