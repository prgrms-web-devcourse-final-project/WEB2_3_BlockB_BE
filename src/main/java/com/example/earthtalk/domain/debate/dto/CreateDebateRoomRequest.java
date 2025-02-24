package com.example.earthtalk.domain.debate.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDebateRoomRequest {
	private Long newsId;

	private String title;

	private News news;

	private String description;

	private MemberNumberType memberNumber;

	private ContinentType continent;

	private CategoryType category;

	private TimeType time;

	private SpeakCountType speakCount;

	private LocalDateTime endTime;
}
