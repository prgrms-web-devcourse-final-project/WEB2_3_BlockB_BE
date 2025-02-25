package com.example.earthtalk.domain.debate.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RoomStatusUpdate {
	private final int roomCount;

	private final Map<String, Integer> proUserCounts;

	private final Map<String, Integer> conUserCounts;

	private final Map<String, Integer> observerUserCounts;
}
