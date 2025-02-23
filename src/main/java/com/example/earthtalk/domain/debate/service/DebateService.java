package com.example.earthtalk.domain.debate.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DebateService {

	private final DebateRepository debateRepository;


	protected Debate getDebateByRoomId(String roomId) {
		return debateRepository.findByUuid(UUID.fromString(roomId))
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
	}
}
