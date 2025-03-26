package com.example.earthtalk.domain.debate.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.dto.DebateMetaDataRoomResponse;
import com.example.earthtalk.domain.debate.dto.DebateRoomResponse;
import com.example.earthtalk.domain.debate.dto.DebateUserResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.debate.store.DebateUserStore;
import com.example.earthtalk.domain.debate.store.ObserverRoomStore;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebateMetaDataService {

	private final DebateRoomStore debateRoomStore;
	private final DebateUserStore debateUserStore;
	private final ObserverRoomStore observerRoomStore;
	private final DebateRepository debateRepository;
	private final UserRepository userRepository;
	private final DebateRoomService debateRoomService;

	/**
	 * 시간 기준 내림차순 정렬된 Debate 목록에서 roomId를 추출하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	@Transactional(readOnly = true)
	public List<DebateMetaDataResponse> getSortByTime() {
		List<Debate> sortedDebates = debateRoomStore.getSortByTime();
		List<String> roomIds = new ArrayList<>();
		for (Debate debate : sortedDebates) {
			roomIds.add(debate.getUuid().toString());
		}
		log.info("getSortByTime - roomIds: {}", roomIds);
		return buildResponseList(roomIds);
	}

	/**
	 * Debater 점수 기준 내림차순 정렬된 roomId 리스트를 이용하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	@Transactional(readOnly = true)
	public List<DebateMetaDataResponse> getSortByDebaterScore() {
		List<String> sortedRoomIds = debateUserStore.getSortedRoomIdsByScoreDesc();
		log.info("getSortByDebaterScore - sortedRoomIds: {}", sortedRoomIds);
		return buildResponseList(sortedRoomIds);
	}

	/**
	 * 현재 시청자 수 기준 내림차순 정렬된 roomId 리스트를 이용하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	@Transactional(readOnly = true)
	public List<DebateMetaDataResponse> getSortByCurrentCount() {
		List<String> sortedRoomIds = observerRoomStore.getSortedRoomIdsByCurrentViewer();
		log.info("getSortByCurrentCount - sortedRoomIds: {}", sortedRoomIds);
		return buildResponseList(sortedRoomIds);
	}

	/**
	 * 최대 시청자 수 기준 내림차순 정렬된 roomId 리스트를 이용하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	@Transactional(readOnly = true)
	public List<DebateMetaDataResponse> getSortByMaxCount() {
		List<String> sortedRoomIds = observerRoomStore.getSortedRoomIdsByMaxViewer();
		log.info("getSortByMaxCount - sortedRoomIds: {}", sortedRoomIds);
		return buildResponseList(sortedRoomIds);
	}

	/**
	 * 공통 헬퍼 메서드: 주어진 roomId 리스트에 대해 DebateRepository, ObserverRoomStore, DebateUserStore의 데이터를 결합하여
	 * DebateMetaDataResponse DTO 리스트를 생성합니다.
	 */
	@Transactional(readOnly = true)
	public List<DebateMetaDataResponse> buildResponseList(List<String> sortedRoomIds) {
		List<DebateMetaDataResponse> responses = new ArrayList<>();
		for (String roomId : sortedRoomIds) {
			log.info("Processing roomId: {}", roomId);
			Optional<Debate> debateOpt = debateRepository.findByUuid(UUID.fromString(roomId));
			if (!debateOpt.isPresent()) {
				log.warn("Debate with roomId {} not found. Skipping.", roomId);
				continue;  // 해당 roomId는 건너뜁니다.
			}
			Debate debate = debateOpt.get();
			Long currentCount = observerRoomStore.getObserverCount(roomId);
			Long maxCount = observerRoomStore.getMaxObserverCount(roomId);

			Set<String> redisProUsers = debateUserStore.getProUsers(roomId);
			Set<String> redisConUsers = debateUserStore.getConUsers(roomId);

			Set<DebateUserResponse> proUserResponses = new HashSet<>();
			for (String userName : redisProUsers) {
				User user = userRepository.findByNickname(userName)
						.orElseThrow(() -> new IllegalArgumentException("User not found for nickname: " + userName));
				proUserResponses.add(DebateUserResponse.builder()
						.id(user.getId())
						.nickname(user.getNickname())
						.email(user.getEmail())
						.position(FlagType.PRO)
						.introduction(user.getIntroduction())
						.defeatNumber(user.getDefeatNumber())
						.winNumber(user.getWinNumber())
						.drawNumber(user.getDrawNumber())
						.profileUrl(user.getProfileUrl())
						.build());
			}

			Set<DebateUserResponse> conUserResponses = new HashSet<>();
			for (String userName : redisConUsers) {
				User user = userRepository.findByNickname(userName)
						.orElseThrow(() -> new IllegalArgumentException("User not found for nickname: " + userName));
				conUserResponses.add(DebateUserResponse.builder()
						.id(user.getId())
						.nickname(user.getNickname())
						.email(user.getEmail())
						.position(FlagType.CON)
						.introduction(user.getIntroduction())
						.defeatNumber(user.getDefeatNumber())
						.winNumber(user.getWinNumber())
						.drawNumber(user.getDrawNumber())
						.profileUrl(user.getProfileUrl())
						.build());
			}

			DebateRoomResponse roomResponse = DebateRoomResponse.builder()
					.uuid(debate.getUuid())
					.title(debate.getTitle())
					.description(debate.getDescription())
					.memberNumberType(debate.getMember().getValue())
					.categoryType(debate.getCategory())
					.continentType(debate.getContinent())
					.newsUrl(debate.getNews() != null ? debate.getNews().getLink() : null)
					.status(debate.getStatus())
					.timeType(debate.getTime().getValue())
					.speakCountType(debate.getSpeakCount().getValue())
					.proUsers(proUserResponses)
					.conUsers(conUserResponses)
					.resultEnabled(debate.isResultEnabled())
					.build();

			DebateMetaDataResponse response = DebateMetaDataResponse.builder()
					.debateRoomResponse(roomResponse)
					.currentCount(currentCount)
					.maxCount(maxCount)
					.build();

			responses.add(response);
		}
		return responses;
	}






	private Set<User> fetchUsersByNames(Collection<String> userNames) {
		log.info("userNames: {}", userNames);
		return userNames.stream()
			.map(userName -> userRepository.findByNickname(userName)
				.orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage())))
			.collect(Collectors.toSet());
	}
}
