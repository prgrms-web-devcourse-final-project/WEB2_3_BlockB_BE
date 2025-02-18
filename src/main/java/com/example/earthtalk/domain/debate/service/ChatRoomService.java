package com.example.earthtalk.domain.debate.service;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.model.ChatRoom;

@Service
public class ChatRoomService {
	private final Map<String, ChatRoom> chatRoomCache = new ConcurrentHashMap<>();

	public void createChatRoom(String roomId, int memberNumberType) {
		ChatRoom chatRoom = ChatRoom.builder()
			.roomId(roomId)
			.memberNumberType(memberNumberType)
			.build();
		chatRoomCache.put(roomId, chatRoom);
	}

	public ChatRoom getChatRoom(String roomId) {
		return chatRoomCache.get(roomId);
	}

	public String CreateRoom() {
		String roomId = UUID.randomUUID().toString();
		return roomId;
	}
}
