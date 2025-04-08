package com.example.earthtalk.domain.debate.component;

import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import com.example.earthtalk.global.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompConnectInterceptor implements ChannelInterceptor {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final NotificationSessionStore notificationSessionStore;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		// StompHeaderAccessor를 통해 메시지 헤더를 래핑
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
		// CONNECT 메시지일 경우만 처리
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String uri = (String) accessor.getSessionAttributes().get("uri");

			if (uri != null && uri.contains("/notification")) {

				log.info("알림 webSocket jwt 인증 시도");

				String token = accessor.getFirstNativeHeader("Authorization");
				if (token != null && token.startsWith("Bearer")) {
					token = jwtTokenProvider.parseBearerToken(token);
					if (jwtTokenProvider.validateAccessToken(token)) {
						String email = jwtTokenProvider.getClaims(token).getSubject();
						User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
						String sessionId = accessor.getSessionId();

						log.info("알림 webSocket jwt 인증 성공 : {}", user.getId());
						log.info("알림 webSocket session : {}", sessionId);

						accessor.getSessionAttributes().put("userId", user.getId());
						notificationSessionStore.registerSession(user.getId(), sessionId);
					}
				}
			} else {
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
		}
		return message;
	}
}
