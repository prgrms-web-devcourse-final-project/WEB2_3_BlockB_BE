package com.example.earthtalk.domain.debate.dto;

import java.util.Set;

import com.example.earthtalk.domain.debate.entity.Debate;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class DebateMetaDataResponse {
	private final Debate debate;
	private final Long currentCount;
	private final Long maxCount;
	private final Set<String> proUsers;
	private final Set<String> conUsers;
}
