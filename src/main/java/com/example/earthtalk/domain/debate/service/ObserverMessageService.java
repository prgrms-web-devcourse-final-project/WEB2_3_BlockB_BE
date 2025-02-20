package com.example.earthtalk.domain.debate.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.ObserverMessage;

/**
 * ObserverMessageService는 관찰자(Observer) 메시지를 WebSocket을 통해 클라이언트에 전송하는 기능을 제공합니다.
 * <p>
 * 이 서비스는 SimpMessagingTemplate을 사용하여, 특정 토론방(roomId)에 연결된 클라이언트에게
 * 관찰자 메시지를 "/topic/observer/{roomId}" 경로로 전송합니다.
 * </p>
 * 현재 아직 구현만 된 코드라 손 볼 부분이 많습니다. 이 부분은 건너뛰셔도 될 것 같습니다.
 */
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
