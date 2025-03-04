package com.example.earthtalk.domain.debate.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.RoomStatusUpdate;
import com.example.earthtalk.domain.debate.service.DebateMetaDataService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RealTimeUpdateController {

	private final DebateMetaDataService debateMetaDataService;

	@MessageMapping("/updateStatus")
	@SendTo("/topic/roomStatus")
	public RoomStatusUpdate sendRoomStatusUpdate() {
		return RoomStatusUpdate.builder()
			.roomCount(debateMetaDataService.getSortByCurrentCount().size())
			.roomSortedByCreatedAt(debateMetaDataService.getSortByTime())
			.roomSortedByUserCount(debateMetaDataService.getSortByDebaterScore())
			.observerCurrent(debateMetaDataService.getSortByCurrentCount())
			.observerMax(debateMetaDataService.getSortByMaxCount())
			.build();
	}
}
