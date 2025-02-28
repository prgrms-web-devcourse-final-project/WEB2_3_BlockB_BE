package com.example.earthtalk.domain.debate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.DebateMetaDataResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.debate.store.DebateUserStore;
import com.example.earthtalk.domain.debate.store.ObserverRoomStore;
import com.example.earthtalk.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DebateMetaDataService {

	private final DebateRoomStore debateRoomStore;
	private final DebateUserStore debateUserStore;
	private final ObserverRoomStore observerRoomStore;
	private final DebateRepository debateRepository;

	/**
	 * 시간 기준 내림차순 정렬된 Debate 목록에서 roomId를 추출하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	public List<DebateMetaDataResponse> getSortByTime() {
		List<Debate> sortedDebates = debateRoomStore.getSortByTime();
		List<String> roomIds = new ArrayList<>();
		for (Debate debate : sortedDebates) {
			roomIds.add(debate.getUuid().toString());
		}
		return buildResponseList(roomIds);
	}

	/**
	 * Debater 점수 기준 내림차순 정렬된 roomId 리스트를 이용하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	public List<DebateMetaDataResponse> getSortByDebaterScore() {
		List<String> sortedRoomIds = debateUserStore.getSortedRoomIdsByScoreDesc();
		return buildResponseList(sortedRoomIds);
	}

	/**
	 * 현재 시청자 수 기준 내림차순 정렬된 roomId 리스트를 이용하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	public List<DebateMetaDataResponse> getSortByCurrentCount() {
		List<String> sortedRoomIds = observerRoomStore.getSortedRoomIdsByCurrentViewer();
		return buildResponseList(sortedRoomIds);
	}

	/**
	 * 최대 시청자 수 기준 내림차순 정렬된 roomId 리스트를 이용하여 집계한 DebateMetaDataResponse 리스트 반환
	 */
	public List<DebateMetaDataResponse> getSortByMaxCount() {
		List<String> sortedRoomIds = observerRoomStore.getSortedRoomIdsByMaxViewer();
		return buildResponseList(sortedRoomIds);
	}

	/**
	 * 공통 헬퍼 메서드: 주어진 roomId 리스트에 대해 DebateRepository, ObserverRoomStore, DebateUserStore의 데이터를 결합하여
	 * DebateMetaDataResponse DTO 리스트를 생성합니다.
	 */
	private List<DebateMetaDataResponse> buildResponseList(List<String> sortedRoomIds) {
		List<DebateMetaDataResponse> responses = new ArrayList<>();
		for (String roomId : sortedRoomIds) {
			Debate debate = debateRepository.findByUuid(UUID.fromString(roomId))
				.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
			Long currentCount = observerRoomStore.getObserverCount(roomId);
			Long maxCount = observerRoomStore.getMaxObserverCount(roomId);
			var proUsers = debateUserStore.getProUsers(roomId);
			var conUsers = debateUserStore.getConUsers(roomId);

			DebateMetaDataResponse response = DebateMetaDataResponse.builder()
				.debate(debate)
				.currentCount(currentCount)
				.maxCount(maxCount)
				.proUsers(proUsers)
				.conUsers(conUsers)
				.build();
			responses.add(response);
		}
		return responses;
	}
}
