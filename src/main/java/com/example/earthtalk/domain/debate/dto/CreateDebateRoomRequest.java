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
public class CreateDebateRoomRequest {
	private Long newsId;
	private String title;
	private String newsUrl;
	private String description;
	private MemberNumberType memberNumber;
	private ContinentType continent;
	private CategoryType category;
	private TimeType time;
	private SpeakCountType speakCount;
	private boolean resultEnabled;

	public static CreateDebateRoomRequest fromEntity(Debate debate) {
		// News 엔티티 가져오기
		News news = debate.getNews();
		if (news != null) {
			// 명시적으로 초기화 (세션이 열려 있는 상태에서 호출되어야 함)
			Hibernate.initialize(news);
		}

		return CreateDebateRoomRequest.builder()
			.newsId(news != null ? news.getId() : null)
			.title(debate.getTitle())
			.newsUrl(news != null ? news.getLink() : "")
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
