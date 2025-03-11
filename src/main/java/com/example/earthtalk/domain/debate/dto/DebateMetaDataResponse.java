package com.example.earthtalk.domain.debate.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class DebateMetaDataResponse {
	private final DebateRoomResponse debateRoomResponse;
	private final Long currentCount;
	private final Long maxCount;
}
