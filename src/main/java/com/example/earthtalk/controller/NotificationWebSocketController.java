package com.example.earthtalk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/noti")
    public void getSession(@Payload String tempId, SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        log.info("sessionId 매칭 tempId : {} | sessionId : {}", tempId, sessionId);
        messagingTemplate.convertAndSend("/queue/handshake-" + tempId, "test session");
        messagingTemplate.convertAndSend("/queue/handshake-" + tempId, sessionId);
    }
}
