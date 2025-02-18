package com.example.earthtalk.domain.debate.handler;

import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;

@Controller
public class ChatWebSocketHandler {

	@MessageMapping("/debate/{roomId}")
	@SendTo("/topic/debate/{roomId}")
	public DebateMessage sendDebateMessage(@DestinationVariable String roomId, @Payload DebateMessage message) {
		message.setRoomId(roomId);

		if (message.getTimestamp() == null || message.getTimestamp().isEmpty()) {
			message.setTimestamp(Instant.now().toString());
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
