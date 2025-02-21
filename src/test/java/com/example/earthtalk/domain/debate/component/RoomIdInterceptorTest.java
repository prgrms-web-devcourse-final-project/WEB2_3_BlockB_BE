package com.example.earthtalk.domain.debate.component;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

public class RoomIdInterceptorTest {

	private RoomIdInterceptor interceptor;

	@BeforeEach
	public void setUp() {
		interceptor = new RoomIdInterceptor();
	}

	@Test
	public void testBeforeHandshake_ServletRequest() throws Exception {
		// given: HttpServletRequest 모의 객체 생성 및 URI 설정
		HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
		// 실제 요청 URI: http://localhost/debate/room123
		URI uri = new URI("http://localhost/debate/room123");
		Mockito.when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(uri.toString()));
		Mockito.when(servletRequest.getRequestURI()).thenReturn("/debate/room123");

		// ServletServerHttpRequest를 생성하고 getURI() 메서드 재정의
		ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest) {
			@Override
			public URI getURI() {
				return uri;
			}
		};

		ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
		WebSocketHandler wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<>();

		// when: beforeHandshake 호출
		boolean proceed = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		// then: 핸드셰이크가 계속 진행되어야 하고, attributes에 roomId가 "room123"으로 저장되어야 함
		assertTrue(proceed);
		assertEquals("room123", attributes.get("roomId"));
	}

	@Test
	public void testBeforeHandshake_NonServletRequest() throws Exception {
		// given: ServerHttpRequest가 ServletServerHttpRequest가 아닌 경우
		ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
		ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
		WebSocketHandler wsHandler = Mockito.mock(WebSocketHandler.class);
		Map<String, Object> attributes = new HashMap<>();

		// when: beforeHandshake 호출
		boolean proceed = interceptor.beforeHandshake(request, response, wsHandler, attributes);

		// then: attributes에는 아무런 값도 추가되지 않아야 함
		assertTrue(proceed);
		assertTrue(attributes.isEmpty());
	}

	@Test
	public void testAfterHandshake() throws Exception {
		// given: afterHandshake는 별다른 작업을 수행하지 않으므로, 단순히 예외 없이 호출 가능해야 함.
		ServerHttpRequest request = Mockito.mock(ServerHttpRequest.class);
		ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);
		WebSocketHandler wsHandler = Mockito.mock(WebSocketHandler.class);
		Exception ex = null;

		// when & then: afterHandshake 호출 시 예외가 발생하지 않아야 함
		assertDoesNotThrow(() -> interceptor.afterHandshake(request, response, wsHandler, ex));
	}
}
