package com.example.earthtalk.domain.debate.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.earthtalk.domain.debate.component.ContinentHandshakeInterceptor;
import com.example.earthtalk.domain.debate.component.RoomIdInterceptor;
import com.example.earthtalk.global.security.util.JwtTokenProvider;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
		log.info("Message Broker configured: simple broker '/topic', application destination prefix '/app'");
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

		registry.addEndpoint("/room-list")
			.setAllowedOrigins("*");
		log.info("Registered STOMP endpoint: /room-list");

		registry.addEndpoint("/room-list/filtered")
			.setAllowedOrigins("*")
			.addInterceptors(new ContinentHandshakeInterceptor());
	}

}
