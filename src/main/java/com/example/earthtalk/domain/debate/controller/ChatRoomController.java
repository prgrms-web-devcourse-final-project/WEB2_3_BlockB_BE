package com.example.earthtalk.domain.debate.controller;

import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;

/**
 * ChatRoomController는 REST API를 통해 토론방(채팅방)을 생성하는 엔드포인트를 제공합니다.
 * <p>
 * 클라이언트로부터 토론방 생성 요청을 받아 {@link DebateRoomService}를 통해 새로운 채팅방을 생성하고,
 * 생성된 채팅방의 고유 식별자(roomId)를 응답으로 반환합니다.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {
	private final DebateRoomService debateRoomService;

	/**
	 * 클라이언트로부터 토론방 생성 요청을 받아 새로운 채팅방을 생성합니다.
	 * <p>
	 * 요청 본문에 담긴 {@link CreateDebateRoomRequest} 정보를 사용하여 {@link DebateRoomService#createDebateRoom(CreateDebateRoomRequest)}
	 * 메서드를 호출하고, 생성된 채팅방의 고유 식별자(roomId)를 HTTP 200 응답과 함께 반환합니다.
	 * </p>
	 *
	 * @param request 토론방 생성에 필요한 정보를 담고 있는 {@link CreateDebateRoomRequest} 객체
	 * @return 생성된 채팅방의 고유 식별자를 포함하는 HTTP 응답 (200 OK)
	 */
	@PostMapping("/create")
	public ResponseEntity<ApiResponse<String>> createRoom(@RequestBody CreateDebateRoomRequest request) {
		String roomId = debateRoomService.createDebateRoom(request);
		return ResponseEntity.ok(ApiResponse.createSuccess(roomId));
	}

}
