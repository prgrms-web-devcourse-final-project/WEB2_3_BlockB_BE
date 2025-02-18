package com.example.earthtalk.domain.debate.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.ObserverMessage;

@Service
public class ObserverMessageService {
	private final SimpMessagingTemplate messagingTemplate;

	public ObserverMessageService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void sendObserverMessage(ObserverMessage message) {
		String destination = "/topic/observer/" + message.getRoomId();
		messagingTemplate.convertAndSend(destination, message);
	}
}
