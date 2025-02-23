package com.example.earthtalk.domain.debate.service;

import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.DebateRole;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.model.DebateRoom;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;

import jakarta.transaction.Transactional;

/**
 * DebateManagementService는 채팅방이 가득 찼을 때 해당 채팅방의 메타데이터를 기반으로
 * Debate 엔티티와 DebateParticipants 엔티티들을 생성하여 데이터베이스에 저장하는 기능을 제공합니다.
 * <p>
 * 이 서비스는 채팅방 캐시에 저장된 {@link DebateRoom} 정보를 활용하여,
 * 토론 방이 꽉 찼을 경우(모든 찬성/반대 사용자가 입장한 경우) 해당 정보를 DB에 영구적으로 저장하고,
 * 이후 캐시에서 해당 채팅방 정보를 제거합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DebateManagementService {

	private final DebateParticipantsRepository debateParticipantsRepository;
	private final DebateChatRepository debateChatRepository;
	private final DebateRepository debateRepository;
	private final UserRepository userRepository;
	private final DebateRoomService debateRoomService;

	/**
	 * 주어진 roomId에 해당하는 채팅방의 캐시 정보가 존재하고, 채팅방이 꽉 찼다면,
	 * 해당 채팅방의 메타데이터를 기반으로 Debate 엔티티를 생성하여 DB에 저장하고,
	 * 찬성(pro) 및 반대(con) 사용자들을 DebateParticipants 엔티티로 생성하여 저장합니다.
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
		DebateRoom debateRoom = debateRoomService.getDebateRoom(roomId);
		if (debateRoom != null && debateRoom.isFull()) {
			Debate debate = Debate.builder()
				.title(debateRoom.getTitle())
				.uuid(UUID.fromString(roomId))
				.description(debateRoom.getSubtitle())
				.member(debateRoom.getMemberNumberType())
				.continent(debateRoom.getContinent())
				.category(debateRoom.getCategory())
				.time(debateRoom.getTime())
				.status(RoomType.DEBATE)
				.agreeNumber(0L)
				.disagreeNumber(0L)
				.build();
			debateRepository.save(debate);

			for (String username : proUserNames) {
				User user = userRepository.findByNickname(username)
					.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
				DebateParticipants debateParticipants = DebateParticipants.builder()
					.debate(debate)
					.user(user)
					.role(DebateRole.PARTICIPANT)
					.position(FlagType.PRO)
					.build();
				debateParticipantsRepository.save(debateParticipants);
			}

			for (String username : conUserNames) {
				User user = userRepository.findByNickname(username)
					.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
				DebateParticipants debateParticipants = DebateParticipants.builder()
					.debate(debate)
					.user(user)
					.role(DebateRole.PARTICIPANT)
					.position(FlagType.CON)
					.build();
				debateParticipantsRepository.save(debateParticipants);
			}
		}
		debateRoomService.removeDebateRoom(roomId);

	}

}
