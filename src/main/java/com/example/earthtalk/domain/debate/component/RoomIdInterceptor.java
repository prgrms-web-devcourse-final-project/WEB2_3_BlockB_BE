package com.example.earthtalk.domain.debate.component;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class RoomIdInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest) {
			ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
			String uri = servletRequest.getURI().toString();

			String roomId = uri.substring(uri.lastIndexOf("/") + 1);
			attributes.put("roomId", roomId);
		}
		return true;
	}


	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}
