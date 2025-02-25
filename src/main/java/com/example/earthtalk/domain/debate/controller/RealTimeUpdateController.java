package com.example.earthtalk.domain.debate.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.earthtalk.domain.debate.dto.RoomStatusUpdate;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.debate.store.DebateUserStore;
import com.example.earthtalk.domain.debate.store.ObserverRoomStore;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RealTimeUpdateController {

	private final DebateRoomStore debateRoomStore;
	private final DebateUserStore debateUserStore;
	private final ObserverRoomStore observerRoomStore;

	@MessageMapping("/updateStatus")
	@SendTo("/topic/roomStatus")
	public RoomStatusUpdate sendRoomStatusUpdate() {
		return RoomStatusUpdate.builder()
			.roomCount(debateRoomStore.getAll().size())
			.proUserCounts(debateUserStore.getProUserCounts())
			.conUserCounts(debateUserStore.getConUserCounts())
			.observerUserCounts(observerRoomStore.getObserverCounts())
			.build();
	}
}
