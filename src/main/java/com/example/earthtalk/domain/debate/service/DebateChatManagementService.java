package com.example.earthtalk.domain.debate.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * DebateChatService는 토론방의 채팅 메시지를 DebateChat 엔티티로 변환하여 데이터베이스에 저장하는 기능을 제공합니다.
 * <p>
 * 이 서비스는 DebateService를 통해 토론방(Debate) 정보를 조회하고, DebateUserService를 통해 사용자의 정보를 확인한 후,
 * DebateMessage의 이벤트가 "chat"인 경우에만 해당 메시지를 DebateChat 엔티티로 매핑하여 DebateChatRepository를 통해 일괄 저장합니다.
 * 만약 해당 메시지에 대응하는 DebateUser가 존재하지 않으면, 해당 메시지는 무시됩니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DebateChatManagementService {

	private final DebateChatRepository debateChatRepository;
	private final DebateService debateService;
	private final DebateParticipantsRepository debateParticipantsRepository;

	/**
	 * 주어진 토론방(UUID)와 DebateMessage 리스트를 기반으로 채팅 로그를 데이터베이스에 저장합니다.
	 * <p>
	 * 처리 과정:
	 * <ol>
	 *   <li>주어진 uuid를 이용하여 Debate 엔티티를 조회합니다.</li>
	 *   <li>DebateMessage 리스트를 스트림으로 순회하면서, 이벤트가 "chat"인 메시지에 대해 다음 작업을 수행합니다:
	 *     <ul>
	 *       <li>DebateUserService를 이용하여 해당 메시지의 userName에 해당하는 DebateParticipants 객체를 조회합니다.</li>
	 *       <li>메시지의 position 값에 따라 FlagType을 결정합니다. ("pro" → {@link FlagType#PRO}, "con" → {@link FlagType#CON}, 그 외 → {@link FlagType#NO_POSITION})</li>
	 *       <li>만약 DebateUser가 존재하지 않으면, 해당 메시지는 무시됩니다.</li>
	 *       <li>DebateChat 엔티티를 Builder 패턴을 이용해 생성합니다.</li>
	 *     </ul>
	 *   </li>
	 *   <li>생성된 DebateChat 엔티티들을 리스트로 수집한 후, DebateChatRepository를 통해 일괄 저장합니다.</li>
	 * </ol>
	 * </p>
	 *
	 * @param uuid     토론방을 식별하기 위한 UUID 문자열
	 * @param messages 해당 토론방에서 발생한 DebateMessage 리스트
	 */
	@Async
	public void saveChatHistory(String uuid, List<DebateMessage> messages) {
		// 시작 로깅: 메서드 진입 및 전달된 메시지 수
		log.info("saveChatHistory 시작: roomId = {}, 메시지 수 = {}", uuid, messages != null ? messages.size() : 0);

		Debate debate = debateService.getDebateByRoomId(uuid);
		log.info("Debate 조회 완료: {}", debate);

		List<DebateChat> chatList = messages.stream()
			.filter(message -> "chat".equals(message.getEvent()))
			.map(message -> {
				DebateParticipants debateParticipants = findDebateUserByUserName(UUID.fromString(uuid), message.getUserName());
				if (debateParticipants == null) {
					// DebateParticipants가 없는 경우 경고 로그 출력
					log.warn("DebateParticipants 없음: roomId = {}, userName = {}", uuid, message.getUserName());
					return Optional.<DebateChat>empty();
				}
				DebateChat debateChat = DebateChat.builder()
					.debate(debate)
					.debateParticipants(debateParticipants)
					.content(message.getMessage())
					.time(message.getTimestamp())
					.build();
				// 생성된 DebateChat 객체를 디버그 레벨로 로깅
				log.debug("DebateChat 생성: {}", debateChat);
				return Optional.of(debateChat);
			})
			.flatMap(Optional::stream)
			.toList();

		log.info("변환된 DebateChat 총 수: {}", chatList.size());

		int batchSize = 100;
		for (int i = 0; i < chatList.size(); i += batchSize) {
			int end = Math.min(i + batchSize, chatList.size());
			List<DebateChat> batch = chatList.subList(i, end);
			log.info("배치 저장 시작: 인덱스 {}부터 {}까지, 배치 크기 = {}", i, end, batch.size());
			debateChatRepository.saveAll(batch);
			log.info("배치 저장 완료: 인덱스 {}부터 {}까지", i, end);
			debateChatRepository.flush();
			log.debug("DB flush 완료: 인덱스 {}부터 {}까지", i, end);
		}

		log.info("saveChatHistory 완료: roomId = {}", uuid);
	}


	private DebateParticipants findDebateUserByUserName(UUID uuid, String userName) {
		return debateParticipantsRepository.findByDebate_UuidAndUser_Nickname(uuid, userName)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
	}
}
