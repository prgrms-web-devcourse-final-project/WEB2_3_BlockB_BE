package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.FlagType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DebateUserResponse {
	private final Long userId;
	private final String userName;
	private final FlagType flagType;
	private final Long win;
	private final Long loss;
	private final Long draw;
}
