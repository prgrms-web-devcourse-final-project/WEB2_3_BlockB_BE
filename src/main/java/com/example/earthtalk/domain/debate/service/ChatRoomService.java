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

@Service
public class ChatRoomService {
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
