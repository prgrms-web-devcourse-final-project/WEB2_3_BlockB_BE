package com.example.earthtalk.domain.debate.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

import com.example.earthtalk.domain.debate.entity.Debate;

@Getter
@Builder
public class RoomStatusUpdate {
	private final int roomCount;

	private final List<Debate> roomSortedByCreatedAt;

	private final List<Debate> roomSortedByUserCount;

	private final Map<String, Integer> proUserCounts;

	private final Map<String, Integer> conUserCounts;

	private final List<DebateObserverResponse> observerCurrent;

	private final List<DebateObserverResponse> observerMax;
}
