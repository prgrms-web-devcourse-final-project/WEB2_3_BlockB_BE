package com.example.earthtalk.domain.debate.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.service.ChatRoomService;

@RestController
@RequestMapping("/api/chat")
public class ChatRoomController {
	private final ChatRoomService chatRoomService;
	private final ApplicationEventPublisher eventPublisher;

	public ChatRoomController(ChatRoomService chatRoomService, ApplicationEventPublisher eventPublisher) {
		this.chatRoomService = chatRoomService;
		this.eventPublisher = eventPublisher;
	}

	@PostMapping("/create")
	public ResponseEntity<String> createRoom(@RequestBody CreateDebateRoomRequest request) {
		String roomId = chatRoomService.createChatRoom(request);

		return ResponseEntity.ok(roomId);
	}

}
