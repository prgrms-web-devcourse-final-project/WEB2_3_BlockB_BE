package com.example.earthtalk.domain.debate.service;

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

/**
 * DebateManagementService는 채팅방이 가득 찼을 때 해당 채팅방의 메타데이터를 기반으로
 * Debate 엔티티와 DebateUser 엔티티들을 생성하여 데이터베이스에 저장하는 기능을 제공합니다.
 * <p>
 * 이 서비스는 채팅방 캐시에 저장된 {@link ChatRoom} 정보를 활용하여,
 * 토론 방이 꽉 찼을 경우(모든 찬성/반대 사용자가 입장한 경우) 해당 정보를 DB에 영구적으로 저장하고,
 * 이후 캐시에서 해당 채팅방 정보를 제거합니다.
 * </p>
 */
@Service
public class DebateManagementService {

	private final DebateUserRepository debateUserRepository;
	private final DebateChatRepository debateChatRepository;
	private final DebateRepository debateRepository;
	private final UserRepository userRepository;
	private final ChatRoomService chatRoomService;

	/**
	 * DebateManagementService 생성자.
	 *
	 * @param debateUserRepository DebateUser 엔티티의 데이터 접근을 위한 리포지토리
	 * @param debateChatRepository Debate 채팅 관련 데이터 접근을 위한 리포지토리
	 * @param debateRepository     Debate 엔티티의 데이터 접근을 위한 리포지토리
	 * @param userRepository       사용자(User) 엔티티의 데이터 접근을 위한 리포지토리
	 * @param chatRoomService      채팅방 정보를 관리하는 ChatRoomService
	 */
	public DebateManagementService(DebateUserRepository debateUserRepository, DebateChatRepository debateChatRepository,
		DebateRepository debateRepository, UserRepository userRepository, ChatRoomService chatRoomService) {
		this.debateUserRepository = debateUserRepository;
		this.debateChatRepository = debateChatRepository;
		this.debateRepository = debateRepository;
		this.userRepository = userRepository;
		this.chatRoomService = chatRoomService;
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방의 캐시 정보가 존재하고, 채팅방이 꽉 찼다면,
	 * 해당 채팅방의 메타데이터를 기반으로 Debate 엔티티를 생성하여 DB에 저장하고,
	 * 찬성(pro) 및 반대(con) 사용자들을 DebateUser 엔티티로 생성하여 저장합니다.
	 * <p>
	 * 모든 사용자(찬성, 반대)가 최대 인원수에 도달하면 캐시에서 해당 채팅방 정보를 제거합니다.
	 * </p>
	 *
	 * @param roomId         토론방의 고유 식별자
	 * @param proUserNames   찬성 사용자들의 닉네임을 포함하는 Set
	 * @param conUserNames   반대 사용자들의 닉네임을 포함하는 Set
	 * @throws BadRequestException  사용자 정보를 조회할 때 해당 닉네임에 해당하는 사용자가 없으면 발생
	 */
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
