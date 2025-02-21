package com.example.earthtalk.domain.debate.component;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 연결 시 특정 토론방의 roomId를 추출하여 세션 속성에 저장하는 인터셉터 클래스.
 *
 * <p>WebSocket 엔드포인트가 `/debate/{roomId}` 와 같은 형태일 때, URL에서 `roomId` 값을 추출하여
 * 해당 WebSocket 세션의 attributes에 추가함.</p>
 *
 * <p>이를 통해 이후 WebSocket 핸들러에서 `roomId` 값을 활용할 수 있음.</p>
 */
@Component
public class RoomIdInterceptor implements HandshakeInterceptor {

	/**
	 * WebSocket 핸드셰이크 요청이 발생하기 전에 실행되며, 요청 URI에서 `roomId` 값을 추출하여 WebSocket 세션 속성에 추가한다.
	 *
	 * @param request     클라이언트의 WebSocket 연결 요청 객체
	 * @param response    서버의 WebSocket 응답 객체
	 * @param wsHandler   WebSocket 핸들러
	 * @param attributes  WebSocket 세션에 저장되는 속성 맵
	 * @return            핸드셰이크를 계속 진행할지 여부 (`true`: 계속 진행, `false`: 차단)
	 * @throws Exception  URI에서 `roomId`를 추출하는 과정에서 발생할 수 있는 예외
	 */
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

	/**
	 * WebSocket 핸드셰이크가 완료된 후 실행되는 메서드.
	 *
	 * <p>현재 구현에서는 특별한 후처리 작업을 수행하지 않음.</p>
	 *
	 * @param request    클라이언트의 WebSocket 요청 객체
	 * @param response   서버의 WebSocket 응답 객체
	 * @param wsHandler  WebSocket 핸들러
	 * @param exception  핸드셰이크 과정에서 발생한 예외 (없을 경우 `null`)
	 */
	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
		Exception exception) {

	}
}
