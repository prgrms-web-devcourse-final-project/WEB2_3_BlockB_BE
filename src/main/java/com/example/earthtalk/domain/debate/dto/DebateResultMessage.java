package com.example.earthtalk.domain.debate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateResultMessage {
	private String event;
	private String roomId;
	private String message;
}
