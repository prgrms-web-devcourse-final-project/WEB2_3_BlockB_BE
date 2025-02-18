package com.example.earthtalk.domain.debate.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateUser;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.repository.DebateUserRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;

import jakarta.transaction.Transactional;

@Service
public class DebateManagementService {

	private final DebateUserRepository debateUserRepository;
	private final DebateChatRepository debateChatRepository;
	private final DebateRepository debateRepository;
	private final UserRepository userRepository;

	public DebateManagementService(DebateUserRepository debateUserRepository, DebateChatRepository debateChatRepository,
		DebateRepository debateRepository, UserRepository userRepository) {
		this.debateUserRepository = debateUserRepository;
		this.debateChatRepository = debateChatRepository;
		this.debateRepository = debateRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void createAndProcessDebate(Debate debate, List<Long> proUserIds, List<Long> conUserIds) {
		if (proUserIds.size() != 1 && proUserIds.size() != 3) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST);
		}
		if (conUserIds.size() != 1 && conUserIds.size() != 3) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST);
		}

		Debate savedDebate = debateRepository.save(debate);
		debateRepository.flush();

		List<DebateUser> debateUsers = new ArrayList<>();

		for (Long userId : proUserIds) {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
			DebateUser debateUser = DebateUser.builder()
				.debate(savedDebate)
				.user(user)
				.position(FlagType.PRO)
				.build();
			debateUsers.add(debateUser);
		}

		for (Long userId : conUserIds) {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
			DebateUser debateUser = DebateUser.builder()
				.debate(savedDebate)
				.user(user)
				.position(FlagType.CON)
				.build();
			debateUsers.add(debateUser);
		}

		debateUserRepository.saveAll(debateUsers);

	}

}
