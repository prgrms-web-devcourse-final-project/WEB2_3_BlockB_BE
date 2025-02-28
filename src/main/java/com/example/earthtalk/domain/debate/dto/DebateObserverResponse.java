package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.Debate;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class DebateObserverResponse {
	private final Debate debate;
	private final Long currentCount;
	private final Long maxCount;
}
