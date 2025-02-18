package com.example.earthtalk.domain.debate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.earthtalk.domain.debate.component.RoomIdInterceptor;

/**
 * WebSocket 및 STOMP 메시지 브로커를 설정하는 클래스.
 *
 * <p>이 클래스는 WebSocket을 통한 실시간 통신을 지원하며, STOMP 프로토콜을 사용하여
 * 메시지를 브로커를 통해 주고받을 수 있도록 설정한다.</p>
 *
 * <ul>
 *   <li>WebSocket 엔드포인트 등록 (`/debate/{roomId}`, `/observer/{roomId}`, `/room-list`)</li>
 *   <li>STOMP 메시지 브로커 설정 (구독 경로: `/topic`, 메시지 전송 경로: `/app`)</li>
 *   <li>`RoomIdInterceptor`를 활용하여 `roomId` 값을 WebSocket 세션에 저장</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	/**
	 * 메시지 브로커를 설정하여 WebSocket 메시지를 효율적으로 관리하도록 구성한다.
	 *
	 * <p>STOMP 기반 메시징 시스템을 사용하며, 클라이언트는 `/app` prefix를 사용하여
	 * 메시지를 전송하고, 구독은 `/topic` 경로를 통해 이루어진다.</p>
	 *
	 * @param config 메시지 브로커 설정 객체
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	/**
	 * WebSocket 엔드포인트를 등록하여 클라이언트가 WebSocket을 통해 연결할 수 있도록 한다.
	 *
	 * <p>각 엔드포인트는 특정 경로에서 WebSocket 연결을 제공하며, `SockJS`를 지원하여
	 * WebSocket을 사용할 수 없는 환경에서도 폴백(fallback) 기능을 제공한다.</p>
	 *
	 * <p>또한, `RoomIdInterceptor`를 추가하여 WebSocket 요청 시 `roomId` 값을 추출하여 세션에 저장한다.</p>
	 *
	 * @param registry STOMP 엔드포인트 등록 객체
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/debate/{roomId}")
			.setAllowedOrigins("*")
			.addInterceptors(new RoomIdInterceptor())
			.withSockJS();

		registry.addEndpoint("/observer/{roomId}")
			.setAllowedOrigins("*")
			.addInterceptors(new RoomIdInterceptor())
			.withSockJS();

		registry.addEndpoint("/room-list")
			.setAllowedOrigins("*")
			.withSockJS();
	}
}
