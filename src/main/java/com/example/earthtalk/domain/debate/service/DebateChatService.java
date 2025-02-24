package com.example.earthtalk.domain.debate.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.entity.DebateUser;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;

import lombok.RequiredArgsConstructor;

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
public class DebateChatService {

	private final DebateChatRepository debateChatRepository;
	private final DebateUserService debateUserService;
	private final DebateService debateService;

	/**
	 * 주어진 토론방(UUID)와 DebateMessage 리스트를 기반으로 채팅 로그를 데이터베이스에 저장합니다.
	 * <p>
	 * 처리 과정:
	 * <ol>
	 *   <li>주어진 uuid를 이용하여 Debate 엔티티를 조회합니다.</li>
	 *   <li>DebateMessage 리스트를 스트림으로 순회하면서, 이벤트가 "chat"인 메시지에 대해 다음 작업을 수행합니다:
	 *     <ul>
	 *       <li>DebateUserService를 이용하여 해당 메시지의 userName에 해당하는 DebateUser 객체를 조회합니다.</li>
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
		Debate debate = debateService.getDebateByRoomId(uuid);

		List<DebateChat> chatList = messages.stream()
			.filter(message -> message.getEvent().equals("chat")) //
			.map(message -> {
				DebateUser debateUser = debateUserService.getDebateUserByUserName(message.getUserName());
				if (debateUser == null) {
					return Optional.<DebateChat>empty(); // Optional 사용하여 null 방지
				}
				// FlagType 변환을 Enum 메서드로 추출하여 가독성 향상
				FlagType flag = FlagType.fromString(message.getPosition());
				return Optional.of(DebateChat.builder()
					.debate(debate)
					.debateUser(debateUser)
					.flagType(flag)
					.content(message.getMessage())
					.time(message.getTime())
					.build());
			})
			.flatMap(Optional::stream) // Optional을 활용하여 null 제거
			.toList();


		int batchSize = 100;
		for (int i = 0; i < chatList.size(); i += batchSize) {
			int end = Math.min(i + batchSize, chatList.size());
			List<DebateChat> batch = chatList.subList(i, end);
			debateChatRepository.saveAll(batch);
			// 필요한 경우 flush()를 호출하여 DB에 즉시 반영할 수 있습니다.
			debateChatRepository.flush();
		}

	}
}
