package com.example.earthtalk.domain.debate.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class DebateMetaDataResponse {
	private final DebateMetaDataRoomResponse debateMetaDataRoomResponse;
	private final Long currentCount;
	private final Long maxCount;
	private final Set<DebateUserResponse> proUsers;
	private final Set<DebateUserResponse> conUsers;
}
