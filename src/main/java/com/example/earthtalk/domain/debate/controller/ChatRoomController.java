package com.example.earthtalk.domain.debate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.earthtalk.domain.debate.dto.CreateRoomRequest;
import com.example.earthtalk.domain.debate.service.ChatRoomService;
import com.example.earthtalk.global.exception.BadRequestException;
import com.example.earthtalk.global.exception.ErrorCode;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {
	private final ChatRoomService chatRoomService;

	public ChatRoomController(ChatRoomService chatRoomService) {
		this.chatRoomService = chatRoomService;
	}

	@PostMapping("/create")
	public ResponseEntity<String> createRoom(@RequestBody CreateRoomRequest request) {
		int roomMember = request.getMemberNumberType();
		if (roomMember != 1 && roomMember != 3) {
			throw new BadRequestException(ErrorCode.BAD_REQUEST);
		}
		String roomId = chatRoomService.CreateRoom();
		chatRoomService.createChatRoom(roomId, request.getMemberNumberType());
		return ResponseEntity.ok(roomId);
	}
}
