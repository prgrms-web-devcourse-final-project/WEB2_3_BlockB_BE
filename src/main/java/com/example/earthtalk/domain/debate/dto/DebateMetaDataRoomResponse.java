package com.example.earthtalk.domain.debate.dto;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.Hibernate;

@Getter
@Builder
public class DebateMetaDataRoomResponse {
	private String title;
	private String description;
	private MemberNumberType memberNumber;
	private ContinentType continent;
	private CategoryType category;
	private TimeType time;
	private SpeakCountType speakCount;
	private boolean resultEnabled;

	public static DebateMetaDataRoomResponse fromEntity(Debate debate) {

		return DebateMetaDataRoomResponse.builder()
			.title(debate.getTitle())
			.description(debate.getDescription())
			.memberNumber(debate.getMember())
			.continent(debate.getContinent())
			.category(debate.getCategory())
			.time(debate.getTime())
			.speakCount(debate.getSpeakCount())
			.resultEnabled(debate.isResultEnabled())
			.build();
	}
}
