package com.example.earthtalk.domain.debate.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class ObserverMessageServiceTest {

	private SimpMessagingTemplate messagingTemplate;
	private ObserverMessageService observerMessageService;

	@BeforeEach
	public void setUp() {
		messagingTemplate = mock(SimpMessagingTemplate.class);
		observerMessageService = new ObserverMessageService(messagingTemplate);
	}

	@Test
	public void testSendObserverMessage() {
		// ObserverMessage 객체 생성 (필요한 필드는 실제 클래스에 맞게 설정)
		ObserverMessage message = new ObserverMessage();
		message.setRoomId("room123");
		message.setMessage("Test Content"); // 예시로 content 필드가 있다고 가정

		// sendObserverMessage 호출
		observerMessageService.sendObserverMessage(message);

		// destination은 "/topic/observer/{roomId}"로 구성되어야 함
		String expectedDestination = "/topic/observer/room123";
		// messagingTemplate.convertAndSend가 예상한 인자로 호출되었는지 검증
		verify(messagingTemplate, times(1)).convertAndSend(eq(expectedDestination), eq(message));
	}
}
