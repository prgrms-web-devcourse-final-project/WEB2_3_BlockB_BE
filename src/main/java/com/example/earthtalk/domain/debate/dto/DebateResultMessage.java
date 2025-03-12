package com.example.earthtalk.domain.debate.dto;

import org.redisson.api.FunctionLibrary;

import com.example.earthtalk.domain.debate.entity.EventType;
import com.example.earthtalk.domain.debate.entity.FlagType;

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
	private FlagType winner;
	private String message;
}
