package com.example.earthtalk.domain.debate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateUser;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.model.ChatRoom;
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
	private final ChatRoomService chatRoomService;

	public DebateManagementService(DebateUserRepository debateUserRepository, DebateChatRepository debateChatRepository,
		DebateRepository debateRepository, UserRepository userRepository, ChatRoomService chatRoomService) {
		this.debateUserRepository = debateUserRepository;
		this.debateChatRepository = debateChatRepository;
		this.debateRepository = debateRepository;
		this.userRepository = userRepository;
		this.chatRoomService = chatRoomService;
	}

	@Transactional
	public void persistChatRoomIfFull(String roomId, Set<String> proUserNames, Set<String> conUserNames) {
		ChatRoom chatRoom = chatRoomService.getChatRoom(roomId);
		if (chatRoom != null && chatRoom.isFull()) {
			Debate debate = Debate.builder()
				.title(chatRoom.getTitle())
				.description(chatRoom.getSubtitle())
				.member(chatRoom.getMemberNumberType())
				.continent(chatRoom.getContinent())
				.category(chatRoom.getCategory())
				.time(chatRoom.getTime())
				.status(RoomType.DEBATE)
				.agreeNumber(0L)
				.disagreeNumber(0L)
				.build();
			debateRepository.save(debate);

			for (String username : proUserNames) {
				User user = userRepository.findByNickname(username)
					.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
				DebateUser debateUser = DebateUser.builder()
					.debate(debate)
					.user(user)
					.position(FlagType.PRO)
					.build();
				debateUserRepository.save(debateUser);
			}

			for (String username : conUserNames) {
				User user = userRepository.findByNickname(username)
					.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
				DebateUser debateUser = DebateUser.builder()
					.debate(debate)
					.user(user)
					.position(FlagType.CON)
					.build();
				debateUserRepository.save(debateUser);
			}
		}
		chatRoomService.removeChatRoom(roomId);

	}

}
