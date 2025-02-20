package com.example.earthtalk.domain.debate.service;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.model.ChatRoom;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.repository.DebateUserRepository;
import com.example.earthtalk.domain.user.repository.UserRepository;

/**
 * ChatRoomService는 토론방 정보를 메모리 기반 캐시에 저장하고 관리하는 서비스 클래스입니다.
 * <p>
 * 이 클래스는 클라이언트로부터 전달받은 {@link CreateDebateRoomRequest} 정보를 기반으로
 * 고유한 채팅방 식별자(roomId)를 생성하여, 해당 정보를 포함하는 {@link ChatRoom} 객체를 캐시에 저장합니다.
 * 또한, 캐시에서 특정 채팅방 정보를 조회하거나 제거하는 기능을 제공합니다.
 * </p>
 */
@Service
public class ChatRoomService {

	/**
	 * 채팅방 정보를 저장하는 in-memory 캐시.
	 * 키는 채팅방의 고유 식별자(roomId)이며, 값은 해당 채팅방 정보를 담은 {@link ChatRoom} 객체입니다.
	 */
	private final Map<String, ChatRoom> chatRoomCache = new ConcurrentHashMap<>();

	private final DebateRepository debateRepository;
	private final DebateUserRepository debateUserRepository;
	private final UserRepository userRepository;

	public ChatRoomService(DebateRepository debateRepository, DebateUserRepository debateUserRepository,
		UserRepository userRepository) {
		this.debateRepository = debateRepository;
		this.debateUserRepository = debateUserRepository;
		this.userRepository = userRepository;
	}

	/**
	 * 새로운 채팅방을 생성하고, 캐시에 저장합니다.
	 * <p>
	 * {@link CreateDebateRoomRequest} 객체에 담긴 정보를 사용하여 고유한 roomId를 생성한 후,
	 * 해당 정보를 기반으로 {@link ChatRoom} 객체를 생성하여 캐시에 저장합니다.
	 * </p>
	 *
	 * @param request 채팅방 생성에 필요한 메타데이터를 담고 있는 {@link CreateDebateRoomRequest} 객체
	 * @return 생성된 채팅방의 고유 식별자(roomId)
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
		chatRoomCache.put(roomId, chatRoom);
		return roomId;
	}

	public ChatRoom getChatRoom(String roomId) {
		return chatRoomCache.get(roomId);
	}

	public void removeChatRoom(String roomId) {
		chatRoomCache.remove(roomId);
	}
}
