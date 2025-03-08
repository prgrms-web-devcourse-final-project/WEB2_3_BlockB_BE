package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateResultMessage {
	private EventType event;
	private String roomId;
	private String message;
}
