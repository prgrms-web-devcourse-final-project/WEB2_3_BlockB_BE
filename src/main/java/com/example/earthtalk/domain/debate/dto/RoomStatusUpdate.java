package com.example.earthtalk.domain.debate.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class RoomStatusUpdate {
	private final int roomCount;

	private final List<DebateMetaDataResponse> roomSortedByCreatedAt;

	private final List<DebateMetaDataResponse> roomSortedByUserCount;

	private final List<DebateMetaDataResponse> observerCurrent;

	private final List<DebateMetaDataResponse> observerMax;
}
