package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.domain.debate.entity.EventType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.DebateResultMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.DebateMessageStore;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.debate.store.DebateUserStore;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.debate.store.ObserverMessageStore;
import com.example.earthtalk.domain.debate.store.ObserverRoomStore;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.ConflictException;
import com.example.earthtalk.global.exception.SaveFailedException;

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
@Slf4j
public class DebateUserService {

	private final SimpMessagingTemplate messagingTemplate;
	private final DebateManagementService debateManagementService;
	private final DebateParticipantsRepository debateParticipantsRepository;

	// 사용자 상태 관리를 담당하는 별도의 컴포넌트
	private final DebateUserStore debateUserStore;
	private final DebateRoomStore debateRoomStore;
	private final DebateRepository debateRepository;

	private final RedissonClient redissonClient;
	private final DebateMessageStore debateMessageStore;
	private final ObserverMessageStore observerMessageStore;
	private final ObserverChatManagementService observerChatManagementService;
	private final DebateChatManagementService debateChatManagementService;
	private final ObserverRoomStore observerRoomStore;

	/**
	 * 토론방에 사용자를 추가합니다.
	 * <p>
	 * 사용자의 포지션이 "pro" 또는 "con"에 따라 적절한 사용자 집합에 추가하고,
	 * 입장 시 WebSocket 메시지로 클라이언트에 알립니다.
	 * 방의 최대 인원 수는 chatRoom의 MemberNumberType에 의해 제한되며, 허용되는 최대 인원은 1 또는 3입니다.
	 * </p>
	 *
	 * @param debate debateRoom model
	 * @param userName 사용자 이름
	 * @param position 사용자의 포지션 ("pro" 또는 "con")
	 * @throws IllegalArgumentException 최대 인원이 1 또는 3이 아닌 경우, 또는 position이 올바르지 않은 경우 발생
	 * @throws ConflictException        이미 최대 인원 수를 초과한 경우 발생
	 */
	public void addUser(Debate debate, String userName, String position) {
		int maxMembers = debate.getMember().getValue();
		String roomId = debate.getUuid().toString();
		if (maxMembers != 1 && maxMembers != 3) {
			throw new IllegalArgumentException(ErrorCode.METHOD_NOT_ALLOWED.getMessage());
		}
		RLock lock = redissonClient.getLock("debate:lock:" + roomId);

		lock.lock();
		try {
			if ("pro".equalsIgnoreCase(position)) {
				if (debateUserStore.getProUserCounts().getOrDefault(roomId, 0) >= maxMembers) {
					Map<String, String> errorMessage = Map.of(
						"event", "error",
						"roomId", roomId,
						"kickedUserName", userName,
						"message", ErrorCode.TOO_MANY_PARTICIPANTS.getMessage()
					);
					messagingTemplate.convertAndSend("/topic/debate/" + roomId, errorMessage);
					throw new IllegalArgumentException(ErrorCode.TOO_MANY_PARTICIPANTS.getMessage());
				}
				debateUserStore.addProUser(roomId, userName);
			} else if ("con".equalsIgnoreCase(position)) {
				if (debateUserStore.getConUserCounts().getOrDefault(roomId, 0) >= maxMembers) {
					Map<String, String> errorMessage = Map.of(
						"event", "error",
						"roomId", roomId,
						"kickedUserName", userName,
						"message", ErrorCode.TOO_MANY_PARTICIPANTS.getMessage()
					);
					messagingTemplate.convertAndSend("/topic/debate/" + roomId, errorMessage);
					throw new IllegalArgumentException(ErrorCode.TOO_MANY_PARTICIPANTS.getMessage());
				}
				debateUserStore.addConUser(roomId, userName);
			} else if ("observer".equalsIgnoreCase(position)) {
				observerRoomStore.addUser(roomId, userName);
				log.info("room {} 에 observer 참여 : {}", roomId, userName);
			} else {
				throw new IllegalArgumentException(ErrorCode.METHOD_NOT_ALLOWED.getMessage());
			}

			sendUserCountUpdate(roomId);
			sendUserJoinMessage(roomId, userName);

			if (debateUserStore.getProUsers(roomId).size() == maxMembers &&
				debateUserStore.getConUsers(roomId).size() == maxMembers) {
				debateManagementService.persistChatRoomIfFull(
					debate,
					debateUserStore.getProUsers(roomId),
					debateUserStore.getConUsers(roomId)
				);
			}
		} finally {
			lock.unlock();
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
	@Transactional
	public void removeUser(String roomId, String userName) {
		log.info("removeUser 시작 - roomId: {}, userName: {}", roomId, userName);

		RLock lock = redissonClient.getLock("debate:lock:" + roomId);
		lock.lock();
		try {
			boolean removed = false;
			log.info("Lock 획득 완료 - roomId: {}", roomId);

			Debate debate = debateRepository.findByUuid(UUID.fromString(roomId))
				.orElseThrow(() -> {
					log.info("Debate room 조회 실패 - roomId: {}", roomId);
					return new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
				});
			log.info("Debate room 조회 성공 - roomId: {}", roomId);

			Set<String> proSet = debateUserStore.getProUsers(roomId);
			if (proSet.contains(userName)) {
				log.info("Pro 사용자 존재 확인 - roomId: {}, userName: {}", roomId, userName);
				removed = proSet.remove(userName);
				log.info("Pro 사용자 제거 결과 - roomId: {}, userName: {}, removed: {}", roomId, userName, removed);
				if (proSet.isEmpty()) {
					debateUserStore.removeProUsers(roomId);
					log.info("Pro 사용자 집합 비어 있음 - roomId: {} 처리 완료", roomId);
				}
			} else {
				log.info("Pro 사용자에 해당하지 않음 - roomId: {}, userName: {}", roomId, userName);
			}

			Set<String> conSet = debateUserStore.getConUsers(roomId);
			if (conSet.contains(userName)) {
				log.info("Con 사용자 존재 확인 - roomId: {}, userName: {}", roomId, userName);
				removed = conSet.remove(userName) || removed;
				log.info("Con 사용자 제거 결과 - roomId: {}, userName: {}, removed: {}", roomId, userName, removed);
				if (conSet.isEmpty()) {
					debateUserStore.removeConUsers(roomId);
					log.info("Con 사용자 집합 비어 있음 - roomId: {} 처리 완료", roomId);
				}
			} else {
				log.info("Con 사용자에 해당하지 않음 - roomId: {}, userName: {}", roomId, userName);
			}

			if (removed) {
				log.info("사용자 제거 성공 - roomId: {}, userName: {}. 사용자 수 업데이트 및 퇴장 메시지 전송", roomId, userName);
				sendUserCountUpdate(roomId);
				sendUserLeftMessage(roomId, userName);
			} else {
				log.info("사용자 제거 시도 실패 또는 해당 사용자가 존재하지 않음 - roomId: {}, userName: {}", roomId, userName);
			}

			if ((debate.getMember().getValue() != 1
				&& ((proSet.size() <= 1 || conSet.size() <= 1)
				&& debate.getAgreeNumber() == 0
				&& debate.getDisagreeNumber() == 0
				&& debate.getNeutralNumber() == 0))
				|| (debate.getMember().getValue() == 1
				&& (proSet.isEmpty() || conSet.isEmpty()
				&& debate.getAgreeNumber() == 0
				&& debate.getDisagreeNumber() == 0
				&& debate.getNeutralNumber() == 0))) {
				log.info("특정 조건 충족 - 채팅 기록 저장 및 결과 처리 시작 - roomId: {}", roomId);
				List<DebateMessage> debateMessages = debateMessageStore.removeDebateMessages(roomId);
				List<ObserverMessage> observerMessages = observerMessageStore.removeObserverMessages(roomId);
				log.info("메시지 삭제 완료 - debateMessages: {}개, observerMessages: {}개",
					debateMessages != null ? debateMessages.size() : 0,
					observerMessages != null ? observerMessages.size() : 0);
				try {
					if (debateMessages != null && !debateMessages.isEmpty()) {
						log.info("Debate 채팅 기록 저장 시작");
						debateChatManagementService.saveChatHistory(roomId, debateMessages);
					} else {
						log.info("Debate 메시지가 null 또는 비어 있음");
					}

					// Observer 메시지 저장: null 또는 empty 인 경우 처리하지 않음
					if (observerMessages != null && !observerMessages.isEmpty()) {
						log.info("Observer 채팅 기록 저장 시작");
						observerChatManagementService.saveChatHistory(roomId, observerMessages);
					} else {
						log.info("Observer 메시지가 null 또는 비어 있음");
					}
					if (debate.isResultEnabled()) {
						FlagType winningTeam = determineWinningTeam(proSet.size());
						updateParticipantsResult(debate, winningTeam);
						log.info("결과 처리 완료 - roomId: {}, winningTeam: {}", roomId, winningTeam);

						String victoryMsg = winningTeam == FlagType.PRO
							? "한쪽 팀이 중도 퇴장 하여 찬성 팀이 승리했습니다."
							: "한쪽 팀의 중도 퇴장 하여 반대 팀이 승리했습니다.";

						DebateResultMessage victoryMessage = DebateResultMessage.builder()
							.event(EventType.WIN_BY_DEFAULT)
							.roomId(roomId)
							.message(victoryMsg)
							.build();

						messagingTemplate.convertAndSend("/topic/debate/" + roomId, victoryMessage);
						messagingTemplate.convertAndSend("/topic/observer/" + roomId, victoryMessage);
						log.info("승리 메시지 전송 완료 - roomId: {}, message: {}", roomId, victoryMsg);

						debateRoomStore.remove(roomId);
						debateUserStore.removeDebateRoom(roomId);
						observerRoomStore.removeRoom(roomId);
					}
				} catch (Exception e) {
					log.info("채팅 기록 저장 실패 - roomId: {}, error: {}", roomId, e.getMessage());
					throw new SaveFailedException(ErrorCode.SAVE_FAILED);
				}
			}
		} finally {
			lock.unlock();
			log.info("Lock 해제 완료 - roomId: {}", roomId);
		}
	}

	private FlagType determineWinningTeam(int proSize) {
		return proSize <= 1 ? FlagType.CON : FlagType.PRO;
	}

	private void updateParticipantsResult(Debate debate, FlagType winningTeam) {
		for (DebateParticipants participant : debate.getParticipants()) {
			if (participant.getPosition() == winningTeam) {
				participant.getUser().incrementWinNumber();
			} else {
				participant.getUser().incrementDefeatNumber();
			}
			debate.updateRoomType(RoomType.CLOSED);
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
}
