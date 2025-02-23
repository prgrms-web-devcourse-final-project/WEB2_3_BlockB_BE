package com.example.earthtalk.domain.debate.model;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;

import lombok.Getter;

@Getter
public class DebateRoom {
	private final String roomId;
	private final MemberNumberType memberNumberType;
	private final String title;
	private final String subtitle;
	private final TimeType time;
	private final CategoryType category;
	private final ContinentType continent;

	private final Set<Long> participantIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public DebateRoom(String roomId, MemberNumberType memberNumberType, String title, String subtitle, TimeType time, CategoryType category, ContinentType continent) {
		this.roomId = String.valueOf(roomId);
		this.memberNumberType = memberNumberType;
		this.title = title;
		this.subtitle = subtitle;
		this.time = time;
		this.category = category;
		this.continent = continent;
	}


	// 채팅방이 꽉 찼는지 확인
	public boolean isFull() {
		return participantIds.size() >= memberNumberType.getValue();
	}
}

