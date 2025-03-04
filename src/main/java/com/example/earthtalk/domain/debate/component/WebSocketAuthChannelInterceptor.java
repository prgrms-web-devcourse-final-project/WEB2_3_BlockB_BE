package com.example.earthtalk.domain.debate.component;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.security.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			return message;
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
			String token = null;

			if (accessor.getSessionAttributes() != null && accessor.getSessionAttributes().containsKey("token")) {
				token = accessor.getSessionAttributes().get("token").toString();
			}
			if (token == null) {
				String authHeader = accessor.getFirstNativeHeader("Authorization");
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					token = authHeader.substring(7);
				}
			}
			if (token != null && jwtTokenProvider.validateAccessToken(token)) {
				Authentication auth = jwtTokenProvider.getAuthentication(token);
				accessor.setUser(auth);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} else {
				log.error("WebSocket 메시지에서 JWT 토큰이 누락되었거나 유효하지 않습니다.");
				throw new IllegalArgumentException(ErrorCode.INVALID_AUTHORITY_TOKEN.getMessage());
			}
		}
		return message;
	}
}
