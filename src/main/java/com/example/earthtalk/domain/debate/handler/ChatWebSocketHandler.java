package com.example.earthtalk.domain.debate.handler;

import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.global.exception.ErrorCode;

@Controller
public class ChatWebSocketHandler {

	@MessageMapping("/debate/{roomId}")
	@SendTo("/topic/debate/{roomId}")
	public DebateMessage sendDebateMessage(@DestinationVariable String roomId, @Payload DebateMessage message) {

		if (message.getEvent() == null || message.getEvent().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getUserId() == null) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getUserName() == null || message.getUserName().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getPosition() == null || message.getPosition().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}

		return message;
	}

	@MessageMapping("/observer/{roomId}")
	@SendTo("/topic/observer/{roomId}")
	public ObserverMessage sendObserverMessage(@DestinationVariable String roomId, @Payload ObserverMessage message) {
		message.setRoomId(roomId);

		if (message.getTimestamp() == null || message.getTimestamp().isEmpty()) {
			message.setTimestamp(Instant.now().toString());
		}

		return message;
	}
}
