package com.example.earthtalk.domain.debate.service;

import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.ConflictException;

/**
 * DebateUserService는 토론방 내 사용자의 입장, 퇴장 및 상태 업데이트를 관리하는 서비스 클래스입니다.
 * <p>
 * 이 서비스는 찬성(pro)과 반대(con) 입장 그룹으로 사용자를 구분하며, 동시에
 * WebSocket 메시지 전송을 통해 클라이언트에 입장/퇴장 및 사용자 수 변경 이벤트를 알립니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DebateUserService {
	private final SimpMessagingTemplate messagingTemplate;
	private final DebateManagementService debateManagementService;

	private final Map<String, Set<String>> proUsers = new ConcurrentHashMap<>();

	private final Map<String, Set<String>> conUsers = new ConcurrentHashMap<>();
	private final ChatRoomService chatRoomService;
	private final UserRepository userRepository;

	/**
	 * 토론방에 사용자를 추가합니다.
	 * <p>
	 * 사용자의 포지션이 "pro" 또는 "con"에 따라 적절한 사용자 집합에 추가하고,
	 * 입장 시 WebSocket 메시지로 클라이언트에 알립니다.
	 * 방의 최대 인원 수는 roomType 파라미터로 제한하며, roomType은 1 또는 3만 허용됩니다.
	 * </p>
	 *
	 * @param chatRoom chatRoom model
	 * @param userName 사용자 이름
	 * @param position 사용자의 포지션 ("pro" 또는 "con")
	 * @throws IllegalArgumentException maxMembers가 1 또는 3이 아닌 경우, 또는 position이 올바르지 않은 경우 발생
	 * @throws ConflictException        이미 최대 인원 수가 초과된 경우 발생
	 */
	public void addUser(ChatRoom chatRoom, String userName, String position) {
		int maxMembers = chatRoom.getMemberNumberType().getValue();
		String roomId = chatRoom.getRoomId();
		if (maxMembers != 1 && maxMembers != 3) {
			throw new IllegalArgumentException(ErrorCode.METHOD_NOT_ALLOWED.getMessage());
		}
		if ("pro".equalsIgnoreCase(position)) {
			proUsers.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
			if (proUsers.get(roomId).size() < maxMembers) {
				proUsers.get(roomId).add(userName);
				sendUserJoinMessage(roomId, String.valueOf(userName));
			} else {
				throw new ConflictException(ErrorCode.TOO_MANY_PARTICIPANTS);
			}
		}else if ("con".equalsIgnoreCase(position)) {
			conUsers.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
			if (conUsers.get(roomId).size() < maxMembers) {
				conUsers.get(roomId).add(userName);
				sendUserJoinMessage(roomId, userName);
			} else {
				throw new ConflictException(ErrorCode.TOO_MANY_PARTICIPANTS);
			}
		} else {
			throw new IllegalArgumentException("Invalid position: " + position);
		}

		sendUserCountUpdate(roomId);

		if (proUsers.getOrDefault(roomId, Set.of()).size() == maxMembers &&
			conUsers.getOrDefault(roomId, Set.of()).size() == maxMembers) {
			debateManagementService.persistChatRoomIfFull(
				roomId,
				proUsers.get(roomId),
				conUsers.get(roomId)
			);
			proUsers.remove(roomId);
			conUsers.remove(roomId);
		}

	}

	/**
	 * 토론방에서 사용자가 퇴장할 때 호출되는 메서드입니다.
	 * <p>
	 * 찬성 또는 반대 사용자 집합에서 해당 사용자를 제거하며,
	 * 사용자가 제거된 경우 클라이언트에 사용자 수 업데이트 및 퇴장 메시지를 전송합니다.
	 * </p>
	 *
	 * @param roomId   토론방 ID
	 * @param userName 퇴장하는 사용자 이름
	 */
	private void sendUserLeftMessage(String roomId, String userName) {
		Map<String, String> message = Map.of(
			"event", "user_left",
			"roomId", roomId,
			"userName", userName,
			"message", userName + "님이 방을 떠났습니다."
		);

		messagingTemplate.convertAndSend("/topic/debate/" + roomId, message);
	}

	/**
	 * 사용자가 토론방에 입장했을 때, 클라이언트에 입장 메시지를 전송합니다.
	 * <p>
	 * 전송되는 메시지는 사용자의 입장을 알리며, "/topic/debate/{roomId}" 경로로 전달됩니다.
	 * </p>
	 *
	 * @param roomId   토론방 ID
	 * @param userName 입장한 사용자 이름
	 */
	private void sendUserJoinMessage(String roomId, String userName) {
		Map<String, String> userJoinedMessage = Map.of(
			"event", "user_joined",
			"roomId", roomId,
			"userName", userName,
			"message", userName + "님이 방에 입장했습니다."
		);

		messagingTemplate.convertAndSend("/topic/debate/" + roomId, userJoinedMessage);
	}

	/**
	 * 토론방에서 사용자가 퇴장할 때 호출되는 메서드입니다.
	 * <p>
	 * 찬성 또는 반대 사용자 집합에서 해당 사용자를 제거하며,
	 * 사용자가 제거된 경우 클라이언트에 사용자 수 업데이트 및 퇴장 메시지를 전송합니다.
	 * </p>
	 *
	 * @param roomId   토론방 ID
	 * @param userName 퇴장하는 사용자 이름
	 */
	public void removeUser(String roomId, String userName) {
		boolean removed = false;

		if (proUsers.containsKey(roomId)) {
			removed = proUsers.get(roomId).remove(userName);
			if (proUsers.get(roomId).isEmpty()) {
				proUsers.remove(roomId);
			}
		}

		if (conUsers.containsKey(roomId)) {
			removed = conUsers.get(roomId).remove(userName) || removed;
			if (conUsers.get(roomId).isEmpty()) {
				conUsers.remove(roomId);
			}
		}

		if (removed) {
			sendUserCountUpdate(roomId);
			sendUserLeftMessage(roomId, userName);
		}
	}


	/**
	 * 주어진 토론방의 현재 사용자 수를 반환합니다.
	 *
	 * @param roomId 토론방 ID
	 * @return "pro"와 "con" 키를 가지는 맵으로, 각 그룹의 사용자 수를 값으로 포함
	 */
	public Map<String, Integer> getUserCount(String roomId) {
		return Map.of(
			"pro", proUsers.getOrDefault(roomId, Set.of()).size(),
			"con", conUsers.getOrDefault(roomId, Set.of()).size()
		);
	}

	/**
	 * 클라이언트에 토론방의 현재 사용자 수를 전송합니다.
	 * <p>
	 * WebSocket을 통해 "/topic/debate/{roomId}" 경로로 사용자 수 정보를 전송합니다.
	 * </p>
	 *
	 * @param roomId 토론방 ID
	 */
	private void sendUserCountUpdate(String roomId) {
		messagingTemplate.convertAndSend("/topic/debate/" + roomId, getUserCount(roomId));
	}

}
