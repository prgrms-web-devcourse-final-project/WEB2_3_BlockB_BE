package com.example.earthtalk.domain.debate.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
import com.example.earthtalk.global.exception.ErrorCode;

@Slf4j
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

	private RoomType status;

	private SpeakCountType speakCount;

	private LocalDateTime cachedTime;

	private Long agreeNumber;

	private Long disagreeNumber;

	private Long neutralNumber;

	private boolean resultEnabled;

	@Builder.Default
	private List<String> participantIds = new ArrayList<>(); // 참가자 ID만 저장

	// Debate 엔티티로부터 RedisDto 생성하는 변환 메서드
	public static DebateRoomRedisDto fromEntity(Debate debate) {
		log.info("fromEntity 호출됨 - Debate 제목: {}, UUID: {}", debate.getTitle(), debate.getUuid());
		DebateRoomRedisDto dto = DebateRoomRedisDto.builder()
			.uuid(debate.getUuid())
			.newsId(debate.getNews() != null ? debate.getNews().getId() : null)
			.title(debate.getTitle())
			.description(debate.getDescription())
			.member(debate.getMember())
			.continent(debate.getContinent())
			.category(debate.getCategory())
			.time(debate.getTime())
			.cachedTime(debate.getCachedTime() != null ? debate.getCachedTime() : LocalDateTime.now())
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
		log.debug("fromEntity 완료 - 생성된 DebateRoomRedisDto: {}", dto);
		return dto;
	}

	public Debate toEntity(NewsRepository newsRepository) {
		log.info("toEntity 호출됨 - DTO UUID: {}", this.uuid);
		Debate.DebateBuilder builder = Debate.builder()
			.uuid(this.uuid)
			.title(this.title)
			.description(this.description)
			.member(this.member)
			.continent(this.continent)
			.category(this.category)
			.cachedTime(this.cachedTime)
			.time(this.time)
			.status(this.status)
			.speakCount(this.speakCount)
			.agreeNumber(this.agreeNumber)
			.disagreeNumber(this.disagreeNumber)
			.neutralNumber(this.neutralNumber)
			.resultEnabled(this.resultEnabled);

		if (this.newsId != null) {
			log.info("toEntity - 뉴스 ID 존재: {}", this.newsId);
			builder.news(
				newsRepository.findById(this.newsId)
					.orElseThrow(() -> {
						log.error("toEntity - 뉴스 조회 실패, newsId: {}", this.newsId);
						return new IllegalArgumentException(ErrorCode.NEWS_NOT_FOUND.getMessage());
					})
			);
		} else {
			log.debug("toEntity - 뉴스 ID 없음, null 처리");
			builder.news(null);
		}

		Debate debate = builder.build();
		log.debug("toEntity 완료 - 생성된 Debate 엔티티: {}", debate);
		return debate;
	}

}
