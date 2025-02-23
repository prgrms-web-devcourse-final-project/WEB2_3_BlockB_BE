package com.example.earthtalk.domain.debate.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.repository.DebateUserRepository;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.domain.debate.store.ChatRoomStore;

/**
 * ChatRoomService는 채팅방 생성 및 관리를 위한 서비스를 제공합니다.
 * <p>
 * 클라이언트로부터 전달받은 {@link CreateDebateRoomRequest} 정보를 기반으로 고유한 채팅방 식별자(roomId)를 생성하고,
 * 해당 정보를 포함하는 {@link ChatRoom} 객체를 생성하여 별도의 저장소({@link ChatRoomStore})에 보관합니다.
 * 또한, 저장소에서 특정 채팅방 정보를 조회하거나 제거하는 기능을 제공합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService {

	private final ChatRoomStore chatRoomStore;
	private final DebateRepository debateRepository;
	private final DebateUserRepository debateUserRepository;
	private final UserRepository userRepository;

	/**
	 * 새로운 채팅방을 생성하고 저장소에 등록합니다.
	 * <p>
	 * {@link CreateDebateRoomRequest} 객체의 정보를 바탕으로 고유한 roomId를 생성한 후,
	 * 해당 정보를 포함하는 {@link ChatRoom} 객체를 생성하여 저장소에 보관합니다.
	 * </p>
	 *
	 * @param request 채팅방 생성에 필요한 메타데이터를 담은 {@link CreateDebateRoomRequest} 객체
	 * @return 생성된 채팅방의 고유 식별자 (roomId)
	 */
	public String createChatRoom(CreateDebateRoomRequest request) {
		String roomId = UUID.randomUUID().toString();
		ChatRoom chatRoom = new ChatRoom(
			roomId,
			request.getMemberNumber(),
			request.getTitle(),
			request.getDescription(),
			request.getTime(),
			request.getCategory(),
			request.getContinent()
		);
		chatRoomStore.put(chatRoom);
		return roomId;
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 반환합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 * @return 해당 채팅방 정보를 담은 {@link ChatRoom} 객체, 존재하지 않으면 null
	 */
	public ChatRoom getChatRoom(String roomId) {
		return chatRoomStore.get(roomId);
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 저장소에서 제거합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 */
	public void removeChatRoom(String roomId) {
		chatRoomStore.remove(roomId);
	}
}
