package com.example.earthtalk.domain.debate.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.DebateRole;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.model.DebateRoom;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.ObserverRoomStore;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObserverUserService {

	private final ObserverRoomStore observerRoomStore;
	private final DebateParticipantsRepository debateParticipantsRepository;
	private final DebateRepository debateRepository;
	private final UserRepository userRepository;

	public void addUser(String roomId, String userName) {
		observerRoomStore.addUser(roomId, userName);
	}

	public void removeUser(String roomId, String userName) {
		observerRoomStore.removeUser(roomId, userName);
	}

	@Transactional
	public DebateParticipants getParticipantByUserName(String roomId, String userName) {
		Optional<DebateParticipants> optional = debateParticipantsRepository.findByDebate_UuidAndUser_Nickname(UUID.fromString(roomId), userName);
		if (optional.isPresent()) {
			return optional.get();
		} else {
			Debate debate = debateRepository.findByUuid(UUID.fromString(roomId))
				.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
			User user = userRepository.findByNickname(userName)
				.orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
			DebateParticipants participant = DebateParticipants.builder()
				.debate(debate)
				.user(user)
				.role(DebateRole.OBSERVER)
				.position(FlagType.NO_POSITION)
				.afterPosition(FlagType.NO_POSITION)
				.build();

			return debateParticipantsRepository.save(participant);
		}
	}

	public int getCurrentObserverCount(String roomId) {
		return observerRoomStore.getObserverCount(roomId);
	}

	public int getMaxObserverCount(String roomId) {
		return observerRoomStore.getMaxObserverCount(roomId);
	}

}
