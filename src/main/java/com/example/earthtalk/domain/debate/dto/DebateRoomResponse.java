package com.example.earthtalk.domain.debate.dto;

import java.util.List;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DebateRoomResponse {
	private final Long roomId;
	private final String title;
	private final String description;
	private final int memberNumberType;
	private final CategoryType categoryType;
	private final ContinentType continentType;
	private final String newsUrl;
	private final RoomType status;
	private final int timeType;
	private final int speakCountType;
	private final List<DebateUserResponse> participants;
}
