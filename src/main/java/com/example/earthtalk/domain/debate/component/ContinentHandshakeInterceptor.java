package com.example.earthtalk.domain.debate.component;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.earthtalk.global.constant.ContinentType;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContinentHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
			String continentParam = httpServletRequest.getParameter("continent");
			if (continentParam != null && !continentParam.trim().isEmpty()) {
				try {
					ContinentType continentType = ContinentType.valueOf(continentParam);
					attributes.put("continentType", continentType);
					log.info("Continent type is {}", continentType);
				} catch (IllegalArgumentException e) {
					log.warn("Invalid continent type: {}", continentParam);
				}
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}
