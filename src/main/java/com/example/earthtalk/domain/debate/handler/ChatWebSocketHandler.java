package com.example.earthtalk.domain.debate.handler;

import java.time.Instant;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.DebateMessage;
import com.example.earthtalk.domain.debate.dto.ObserverMessage;
import com.example.earthtalk.global.exception.ErrorCode;

/**
 * ChatWebSocketHandler는 WebSocket을 통한 토론 및 관찰 메시지 처리를 담당하는 컨트롤러입니다.
 * <p>
 * "/debate/{roomId}" 경로로 들어오는 DebateMessage 메시지를 처리하며, 검증 후 해당 토픽("/topic/debate/{roomId}")으로 브로드캐스트합니다.
 * 또한, "/observer/{roomId}" 경로로 들어오는 ObserverMessage 메시지를 처리하여, roomId와 타임스탬프를 설정한 후 "/topic/observer/{roomId}"로 전송합니다.
 * </p>
 */
@Controller
public class ChatWebSocketHandler {

	/**
	 * 토론 메시지를 처리하여 검증된 DebateMessage를 브로드캐스트합니다.
	 * <p>
	 * 이 메서드는 "/debate/{roomId}" 경로로 들어오는 메시지를 수신하며,
	 * 메시지의 {@code event}, {@code userName}, {@code position}, {@code message} 필드가 null이거나 공백이면
	 * {@code IllegalArgumentException}을 발생시킵니다.
	 * </p>
	 *
	 * @param roomId  대상 토론방의 식별자 (URL 경로 변수)
	 * @param message  클라이언트로부터 전달된 DebateMessage 페이로드
	 * @return 검증 후 그대로 반환된 DebateMessage, 이는 "/topic/debate/{roomId}"로 전송됩니다.
	 * @throws IllegalArgumentException 메시지의 필수 필드가 null 또는 공백인 경우
	 */
	@MessageMapping("/debate/{roomId}")
	@SendTo("/topic/debate/{roomId}")
	public DebateMessage sendDebateMessage(@DestinationVariable String roomId, @Payload DebateMessage message) {

		if (message.getEvent() == null || message.getEvent().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getUserName() == null || message.getUserName().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getPosition() == null || message.getPosition().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}
		if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}

		return message;
	}

	/**
	 * 관찰자 메시지를 처리하여 ObserverMessage를 브로드캐스트합니다.
	 * <p>현재 메시지 처리 기능은 추가적으로 구현해야 하는 상황입니다.</p>
	 * <p>
	 * 이 메서드는 "/observer/{roomId}" 경로로 들어오는 메시지를 수신하며, 전달받은 ObserverMessage 객체의 roomId 필드를
	 * URL의 {roomId} 값으로 설정합니다. 만약 타임스탬프가 null이거나 공백이면 현재 시간으로 설정합니다.
	 * </p>
	 *
	 * @param roomId  대상 토론방의 식별자 (URL 경로 변수)
	 * @param message  클라이언트로부터 전달된 ObserverMessage 페이로드
	 * @return 수정된 ObserverMessage 객체, 이는 "/topic/observer/{roomId}"로 전송됩니다.
	 *
	 *
	 */
	@MessageMapping("/observer/{roomId}")
	@SendTo("/topic/observer/{roomId}")
	public ObserverMessage sendObserverMessage(@DestinationVariable String roomId, @Payload ObserverMessage message) {
		message.setRoomId(roomId);

		if (message.getTimestamp() == null || message.getTimestamp().isEmpty()) {
			message.setTimestamp(Instant.now().toString());
		}

		return message;
	}
}
