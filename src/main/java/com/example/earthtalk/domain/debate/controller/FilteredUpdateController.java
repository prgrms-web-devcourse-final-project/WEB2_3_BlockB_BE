package com.example.earthtalk.domain.debate.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.dto.RoomStatusUpdate;
import com.example.earthtalk.domain.debate.service.DebateMetaDataService;
import com.example.earthtalk.global.constant.ContinentType;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FilteredUpdateController {

	private final DebateMetaDataService debateMetaDataService;

	@MessageMapping("/filteredUpdate")
	@SendTo("/topic/filteredStatus")
	public RoomStatusUpdate sendRoomStatusUpdate(RoomStatusUpdate roomStatusUpdate, SimpMessageHeaderAccessor headerAccessor) {
		ContinentType continentType = (ContinentType) headerAccessor.getSessionAttributes().get("continentType");
		List<DebateMetaDataResponse> filteredByTime = debateMetaDataService.getSortByTime().stream()
			.filter(response -> response.getDebateMetaDataRoomResponse().getContinent().equals(continentType))
			.collect(Collectors.toList());
		List<DebateMetaDataResponse> filteredByScore = debateMetaDataService.getSortByDebaterScore().stream()
			.filter(response -> response.getDebateMetaDataRoomResponse().getContinent().equals(continentType))
			.collect(Collectors.toList());
		List<DebateMetaDataResponse> filteredObserverCurrent = debateMetaDataService.getSortByCurrentCount().stream()
			.filter(response -> response.getDebateMetaDataRoomResponse().getContinent().equals(continentType))
			.collect(Collectors.toList());
		List<DebateMetaDataResponse> filteredObserverMax = debateMetaDataService.getSortByMaxCount().stream()
			.filter(response -> response.getDebateMetaDataRoomResponse().getContinent().equals(continentType))
			.collect(Collectors.toList());

		return RoomStatusUpdate.builder()
			.roomCount(filteredByTime.size())
			.roomSortedByCreatedAt(filteredByTime)
			.roomSortedByUserCount(filteredByScore)
			.observerCurrent(filteredObserverCurrent)
			.observerMax(filteredObserverMax)
			.build();
	}
}
