package com.example.earthtalk.domain.debate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.earthtalk.domain.debate.component.RoomIdInterceptor;
import com.example.earthtalk.domain.debate.component.WebSocketAuthChannelInterceptor;
import com.example.earthtalk.global.security.util.JwtTokenProvider;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
		log.info("Message Broker configured: simple broker '/topic', application destination prefix '/app'");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/debate/{roomId}")
			.setAllowedOrigins("http://localhost:5173")
			.addInterceptors(new RoomIdInterceptor())
			.withSockJS();
		log.info("Registered STOMP endpoint: /debate/{roomId} with SockJS fallback");

		registry.addEndpoint("/observer/{roomId}")
			.setAllowedOrigins("http://localhost:5173")
			.addInterceptors(new RoomIdInterceptor())
			.withSockJS();
		log.info("Registered STOMP endpoint: /observer/{roomId} with SockJS fallback");

		registry.addEndpoint("/room-list")
			.setAllowedOrigins("http://localhost:5173")
			.withSockJS();
		log.info("Registered STOMP endpoint: /room-list with SockJS fallback");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new WebSocketAuthChannelInterceptor(jwtTokenProvider));
		log.info("Configured client inbound channel with WebSocketAuthChannelInterceptor");
	}
}
