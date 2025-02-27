package com.example.earthtalk.domain.debate.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebateMessage {

	private String event;

	private String userName;

	private String position;

	private String message;

	private LocalDateTime timestamp;

	public boolean isValidMessage() {
		return (event != null && !event.trim().isEmpty())
			&& (userName != null && !userName.trim().isEmpty())
			&& (position != null && !position.trim().isEmpty())
			&& (message != null && !message.trim().isEmpty());
	}
}
