package com.example.earthtalk.domain.debate.controller;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.dto.RoomStatusUpdate;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.service.DebateMetaDataService;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.global.constant.ContinentType;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FilteredUpdateController {

	private final DebateMetaDataService debateMetaDataService;

	@MessageMapping("/filteredUpdate")
	@SendTo("/topic/filteredStatus")
	public RoomStatusUpdate sendRoomStatusUpdate(SimpMessageHeaderAccessor headerAccessor) {
		ContinentType continentType = (ContinentType) headerAccessor.getSessionAttributes().get("continentType");
		CategoryType categoryType = (CategoryType) headerAccessor.getSessionAttributes().get("categoryType");
		MemberNumberType memberNumberType = (MemberNumberType) headerAccessor.getSessionAttributes().get("memberNumberType");

		Predicate<DebateMetaDataResponse> filterPredicate = response -> {
			boolean matches = true;
			if (continentType != null) {
				matches &= response.getDebateMetaDataRoomResponse().getContinent().equals(continentType);
			}
			if (categoryType != null) {
				matches &= response.getDebateMetaDataRoomResponse().getCategory().equals(categoryType);
			}
			if (memberNumberType != null) {
				matches &= response.getDebateMetaDataRoomResponse().getMemberNumber().equals(memberNumberType);
			}
			return matches;
		};

		List<DebateMetaDataResponse> filteredByTime = debateMetaDataService.getSortByTime().stream()
			.filter(filterPredicate)
			.collect(Collectors.toList());
		List<DebateMetaDataResponse> filteredByScore = debateMetaDataService.getSortByDebaterScore().stream()
			.filter(filterPredicate)
			.collect(Collectors.toList());
		List<DebateMetaDataResponse> filteredObserverCurrent = debateMetaDataService.getSortByCurrentCount().stream()
			.filter(filterPredicate)
			.collect(Collectors.toList());
		List<DebateMetaDataResponse> filteredObserverMax = debateMetaDataService.getSortByMaxCount().stream()
			.filter(filterPredicate)
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
