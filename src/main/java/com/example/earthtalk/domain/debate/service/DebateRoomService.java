package com.example.earthtalk.domain.debate.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.global.exception.ErrorCode;

/**
 * ChatRoomService는 채팅방 생성 및 관리를 위한 서비스를 제공합니다.
 * <p>
 * 클라이언트로부터 전달받은 {@link CreateDebateRoomRequest} 정보를 기반으로 고유한 채팅방 식별자(roomId)를 생성하고,
 * 해당 정보를 포함하는 {@link Debate} 객체를 생성하여 별도의 저장소({@link DebateRoomStore})에 보관합니다.
 * 또한, 저장소에서 특정 채팅방 정보를 조회하거나 제거하는 기능을 제공합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DebateRoomService {

	private final DebateRoomStore debateRoomStore;
	private final DebateRepository debateRepository;
	private final NewsRepository newsRepository;

	/**
	 * 새로운 채팅방을 생성하고 저장소에 등록합니다.
	 * <p>
	 * {@link CreateDebateRoomRequest} 객체의 정보를 바탕으로 고유한 roomId를 생성한 후,
	 * 해당 정보를 포함하는 {@link Debate} 객체를 생성하여 저장소에 보관합니다.
	 * </p>
	 *
	 * @param request 채팅방 생성에 필요한 메타데이터를 담은 {@link CreateDebateRoomRequest} 객체
	 * @return 생성된 채팅방의 고유 식별자 (roomId)
	 */
	public String createDebateRoom(CreateDebateRoomRequest request) {
		String roomId = UUID.randomUUID().toString();
		try {
			News news = newsRepository.getReferenceById(request.getNewsId());
			Debate debate = Debate.builder()
				.uuid(UUID.fromString(roomId))
				.news(news)
				.title(request.getTitle())
				.description(request.getDescription())
				.member(request.getMemberNumber())
				.continent(request.getContinent())
				.category(request.getCategory())
				.speakCount(request.getSpeakCount())
				.time(request.getTime())
				.build();
			debateRoomStore.put(debate);
		} catch (Exception e) {
			throw new IllegalArgumentException(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
		}
		return roomId;
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 반환합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 * @return 해당 채팅방 정보를 담은 {@link Debate} 객체, 존재하지 않으면 null
	 */
	public Debate getDebateRoom(String roomId) {
		return debateRoomStore.get(roomId);
	}

	public Debate getDebate(String roomId) {
		return debateRepository.findByUuid(UUID.fromString(roomId))
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 저장소에서 제거합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 */
	public void removeDebateRoom(String roomId) {
		debateRoomStore.remove(roomId);
	}
}
