package com.example.earthtalk.domain.debate.service;

import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.store.DebateUserStore;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.model.DebateRoom;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.ConflictException;

/**
 * DebateUserService는 토론방 내 사용자의 입장, 퇴장 및 상태 업데이트를 관리하는 서비스 클래스입니다.
 * <p>
 * 이 서비스는 찬성(pro)과 반대(con) 입장 그룹으로 사용자를 구분하며, 동시에
 * WebSocket 메시지 전송을 통해 클라이언트에 입장/퇴장 및 사용자 수 변경 이벤트를 알립니다.
 * 사용자 상태 관리를 별도의 ChatUserStore 컴포넌트에 위임하여, 관심사의 분리 및 향후 분산 캐시 전환에 대비합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DebateUserService {

	private final SimpMessagingTemplate messagingTemplate;
	private final DebateManagementService debateManagementService;
	private final DebateRoomService debateRoomService;
	private final UserRepository userRepository;
	private final DebateParticipantsRepository debateParticipantsRepository;

	// 사용자 상태 관리를 담당하는 별도의 컴포넌트
	private final DebateUserStore debateUserStore;

	/**
	 * 토론방에 사용자를 추가합니다.
	 * <p>
	 * 사용자의 포지션이 "pro" 또는 "con"에 따라 적절한 사용자 집합에 추가하고,
	 * 입장 시 WebSocket 메시지로 클라이언트에 알립니다.
	 * 방의 최대 인원 수는 chatRoom의 MemberNumberType에 의해 제한되며, 허용되는 최대 인원은 1 또는 3입니다.
	 * </p>
	 *
	 * @param debateRoom debateRoom model
	 * @param userName 사용자 이름
	 * @param position 사용자의 포지션 ("pro" 또는 "con")
	 * @throws IllegalArgumentException 최대 인원이 1 또는 3이 아닌 경우, 또는 position이 올바르지 않은 경우 발생
	 * @throws ConflictException        이미 최대 인원 수를 초과한 경우 발생
	 */
	public void addUser(DebateRoom debateRoom, String userName, String position) {
		int maxMembers = debateRoom.getMemberNumberType().getValue();
		String roomId = debateRoom.getRoomId();
		if (maxMembers != 1 && maxMembers != 3) {
			throw new IllegalArgumentException(ErrorCode.METHOD_NOT_ALLOWED.getMessage());
		}
		if ("pro".equalsIgnoreCase(position)) {
			Set<String> proSet = debateUserStore.getProUsers(roomId);
			if (proSet.size() < maxMembers) {
				proSet.add(userName);
				sendUserJoinMessage(roomId, userName);
			} else {
				throw new ConflictException(ErrorCode.TOO_MANY_PARTICIPANTS);
			}
		} else if ("con".equalsIgnoreCase(position)) {
			Set<String> conSet = debateUserStore.getConUsers(roomId);
			if (conSet.size() < maxMembers) {
				conSet.add(userName);
				sendUserJoinMessage(roomId, userName);
			} else {
				throw new ConflictException(ErrorCode.TOO_MANY_PARTICIPANTS);
			}
		} else {
			throw new IllegalArgumentException("Invalid position: " + position);
		}

		sendUserCountUpdate(roomId);

		if (debateUserStore.getProUsers(roomId).size() == maxMembers &&
			debateUserStore.getConUsers(roomId).size() == maxMembers) {
			debateManagementService.persistChatRoomIfFull(
				roomId,
				debateUserStore.getProUsers(roomId),
				debateUserStore.getConUsers(roomId)
			);
			debateUserStore.removeProUsers(roomId);
			debateUserStore.removeConUsers(roomId);
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
	public void removeUser(String roomId, String userName) {
		boolean removed = false;

		Set<String> proSet = debateUserStore.getProUsers(roomId);
		if (proSet.contains(userName)) {
			removed = proSet.remove(userName);
			if (proSet.isEmpty()) {
				debateUserStore.removeProUsers(roomId);
			}
		}

		Set<String> conSet = debateUserStore.getConUsers(roomId);
		if (conSet.contains(userName)) {
			removed = conSet.remove(userName) || removed;
			if (conSet.isEmpty()) {
				debateUserStore.removeConUsers(roomId);
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
			"pro", debateUserStore.getProUsers(roomId).size(),
			"con", debateUserStore.getConUsers(roomId).size()
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

	/**
	 * 토론방에 사용자가 입장했을 때, 입장 메시지를 전송합니다.
	 * <p>
	 * 메시지는 "/topic/debate/{roomId}" 경로로 전송되며, 사용자 입장을 알립니다.
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
	 * 토론방에서 사용자가 퇴장할 때, 퇴장 메시지를 전송합니다.
	 * <p>
	 * 메시지는 "/topic/debate/{roomId}" 경로로 전송되며, 사용자 퇴장을 알립니다.
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
	 * 주어진 사용자 이름에 해당하는 DebateParticipants 객체를 반환합니다.
	 * 만약 DebateUser를 찾을 수 없으면, USER_NOT_FOUND 오류 메시지와 함께 예외를 발생시킵니다.
	 *
	 * @param userName 사용자 id
	 * @return DebateParticipants 객체
	 * @throws IllegalArgumentException DebateUser가 존재하지 않을 경우
	 */
	protected DebateParticipants getDebateUserByUserName(String userName) {
		return debateParticipantsRepository.findByUser_Nickname(userName)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
	}
}
