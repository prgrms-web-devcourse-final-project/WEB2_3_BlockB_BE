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

	private LocalDateTime time;
}
