package com.example.earthtalk.domain.debate.config;

import com.example.earthtalk.domain.debate.component.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompConnectInterceptor stompConnectInterceptor;
	private final UserIdInterceptor userIdInterceptor;
	private final CustomHandShakeHandler customHandShakeHandler;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/queue");
		config.setApplicationDestinationPrefixes("/app");
		config.setUserDestinationPrefix("/user");
		log.info("Message Broker configured: simple broker '/topic', application destination prefix '/app', user prefix '/user'");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/debate/{roomId}")
			.setAllowedOrigins("*")
			.addInterceptors(new RoomIdInterceptor());
		log.info("Registered STOMP endpoint: /debate/{roomId}");

		registry.addEndpoint("/observer/{roomId}")
			.setAllowedOrigins("*")
			.addInterceptors(new RoomIdInterceptor());
		log.info("Registered STOMP endpoint: /observer/{roomId}");

		registry.addEndpoint("/room-list/filtered")
			.setAllowedOrigins("*")
			.addInterceptors(new QueryHandshakeInterceptor());

		registry.addEndpoint("/notification")
				.setAllowedOrigins("*")
				.setHandshakeHandler(customHandShakeHandler)
				.addInterceptors(userIdInterceptor);
		log.info("Registered STOMP endpoint: /notification");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompConnectInterceptor);
	}
}
