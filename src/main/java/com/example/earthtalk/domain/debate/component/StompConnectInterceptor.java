package com.example.earthtalk.domain.debate.component;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompConnectInterceptor implements ChannelInterceptor {

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		// StompHeaderAccessor를 통해 메시지 헤더를 래핑
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		// CONNECT 메시지일 경우만 처리
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			// connectHeaders에서 값 추출
			String userName = accessor.getFirstNativeHeader("userName");
			String position = accessor.getFirstNativeHeader("position");
			String roomId = accessor.getFirstNativeHeader("roomId");

			// 추출한 값을 세션 속성에 저장
			if (accessor.getSessionAttributes() != null) {
				if (userName != null && !userName.isEmpty()) {
					accessor.getSessionAttributes().put("userName", userName);
				}
				if (position != null && !position.isEmpty()) {
					accessor.getSessionAttributes().put("position", position);
				}
				if (roomId != null && !roomId.isEmpty()) {
					accessor.getSessionAttributes().put("roomId", roomId);
				}
			}
		}
		return message;
	}
}
