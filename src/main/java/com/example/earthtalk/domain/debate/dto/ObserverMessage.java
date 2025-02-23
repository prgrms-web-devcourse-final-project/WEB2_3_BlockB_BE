package com.example.earthtalk.domain.debate.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObserverMessage {
	private String event;

	private String userName;

	private String message;

	private LocalDateTime timestamp;

}
