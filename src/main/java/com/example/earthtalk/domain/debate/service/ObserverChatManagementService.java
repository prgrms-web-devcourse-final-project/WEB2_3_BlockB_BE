package com.example.earthtalk.domain.debate.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.DebateRole;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.model.DebateRoom;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObserverChatManagementService {

	private final DebateChatRepository debateChatRepository;
	private final DebateRoomService debateRoomService;
	private final ObserverUserService observerUserService;


	@Transactional
	public void saveChatHistory(String roomId, List<ObserverMessage> observerMessages) {
		Debate debate = debateRoomService.getDebate(roomId);
		if (debate == null) {
			throw new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
		}
		List<DebateChat> chatEntities = observerMessages.stream().map(message -> {
			DebateParticipants participants = observerUserService.getParticipantByUserName(roomId, message.getUserName());
			return DebateChat.builder()
				.debate(debate)
				.debateParticipants(participants)
				.content(message.getMessage())
				.time(message.getTimestamp())
				.build();
		}).toList();

		final int batchSize = 100;
		for (int i = 0; i < chatEntities.size(); i++) {
			debateChatRepository.save(chatEntities.get(i));

			if ((i + 1) % batchSize == 0) {
				debateChatRepository.flush();
			}
		}
		debateChatRepository.flush();
	}
}
