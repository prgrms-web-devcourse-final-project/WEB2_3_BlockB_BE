package com.example.earthtalk.domain.debate.store;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * ChatUserStore는 각 채팅방(roomId)별로 찬성(pro) 및 반대(con) 사용자 목록을 관리하는 컴포넌트입니다.
 * <p>
 * 이 클래스는 스레드 안전한 ConcurrentHashMap과 ConcurrentHashMap.newKeySet()을 사용하여
 * 각 채팅방의 사용자 집합을 저장하고, 추가/삭제/조회 기능을 제공합니다.
 * </p>
 */
@Component
public class DebateUserStore {

	private final ConcurrentHashMap<String, Set<String>> proUsers = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Set<String>> conUsers = new ConcurrentHashMap<>();

	/**
	 * 주어진 채팅방 ID에 대한 찬성 사용자 집합을 반환합니다.
	 * 만약 해당 채팅방이 없으면, 새로운 집합을 생성하여 반환합니다.
	 *
	 * @param roomId 채팅방 식별자
	 * @return 찬성 사용자 집합
	 */
	public Set<String> getProUsers(String roomId) {
		return proUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
	}

	/**
	 * 주어진 채팅방 ID에 대한 반대 사용자 집합을 반환합니다.
	 * 만약 해당 채팅방이 없으면, 새로운 집합을 생성하여 반환합니다.
	 *
	 * @param roomId 채팅방 식별자
	 * @return 반대 사용자 집합
	 */
	public Set<String> getConUsers(String roomId) {
		return conUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
	}

	/**
	 * 주어진 채팅방 ID의 찬성 사용자 집합을 제거합니다.
	 *
	 * @param roomId 채팅방 식별자
	 */
	public void removeProUsers(String roomId) {
		proUsers.remove(roomId);
	}

	/**
	 * 주어진 채팅방 ID의 반대 사용자 집합을 제거합니다.
	 *
	 * @param roomId 채팅방 식별자
	 */
	public void removeConUsers(String roomId) {
		conUsers.remove(roomId);
	}

	/**
	 * 각 채팅방별 찬성 사용자 수를 집계하여 반환합니다.
	 *
	 * @return 방 ID와 찬성 사용자 수의 매핑 정보
	 */
	public Map<String, Integer> getProUserCounts() {
		Map<String, Integer> proCounts = new HashMap<>();
		for (String roomId : proUsers.keySet()) {
			proCounts.put(roomId, getProUsers(roomId).size());
		}
		return proCounts;
	}

	/**
	 * 각 채팅방별 반대 사용자 수를 집계하여 반환합니다.
	 *
	 * @return 방 ID와 반대 사용자 수의 매핑 정보
	 */
	public Map<String, Integer> getConUserCounts() {
		Map<String, Integer> conCounts = new HashMap<>();
		for (String roomId : conUsers.keySet()) {
			conCounts.put(roomId, getConUsers(roomId).size());
		}
		return conCounts;
	}
}
