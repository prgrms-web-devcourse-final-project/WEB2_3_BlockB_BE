package com.example.earthtalk.domain.debate.dto;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.constant.ContinentType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "debateRoom", timeToLive = 86400) // 24시간 동안 저장
public class DebateRoomRedisDto implements Serializable {

	private UUID uuid;

	private Long newsId; // News 엔티티의 ID만 저장 (객체 전체 저장 방지)

	private String title;

	private String description;

	private MemberNumberType member;

	private ContinentType continent;

	private CategoryType category;

	private TimeType time;

	private LocalDateTime createdAt;

	private LocalDateTime endTime;

	private RoomType status;

	private SpeakCountType speakCount;

	private Long agreeNumber;

	private Long disagreeNumber;

	private Long neutralNumber;

	private boolean resultEnabled;

	@Builder.Default
	private List<String> participantIds = new ArrayList<>(); // 참가자 ID만 저장

	// Debate 엔티티로부터 RedisDto 생성하는 변환 메서드
	public static DebateRoomRedisDto fromEntity(Debate debate) {
		return DebateRoomRedisDto.builder()
			.uuid(debate.getUuid())
			.newsId(debate.getNews().getId()) // News ID만 저장
			.title(debate.getTitle())
			.description(debate.getDescription())
			.member(debate.getMember())
			.continent(debate.getContinent())
			.category(debate.getCategory())
			.time(debate.getTime())
			.createdAt(LocalDateTime.now())
			.endTime(debate.getEndTime())
			.status(debate.getStatus())
			.speakCount(debate.getSpeakCount())
			.agreeNumber(debate.getAgreeNumber())
			.disagreeNumber(debate.getDisagreeNumber())
			.neutralNumber(debate.getNeutralNumber())
			.resultEnabled(debate.isResultEnabled())
			.participantIds(debate.getParticipants().stream()
				.map(p -> p.getId().toString())
				.collect(Collectors.toList()))
			.build();
	}

	// RedisDto를 Debate 엔티티로 변환하는 메서드
	public Debate toEntity(NewsRepository newsRepository) {
		return Debate.builder()
			.uuid(this.uuid)
			.news(newsRepository.findById(this.newsId).orElseThrow())
			.title(this.title)
			.description(this.description)
			.member(this.member)
			.continent(this.continent)
			.category(this.category)
			.time(this.time)
			.endTime(this.endTime)
			.status(this.status)
			.speakCount(this.speakCount)
			.agreeNumber(this.agreeNumber)
			.disagreeNumber(this.disagreeNumber)
			.neutralNumber(this.neutralNumber)
			.resultEnabled(this.resultEnabled)
			.build();
	}
}
