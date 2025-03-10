package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.domain.debate.dto.VoteResponse;
import com.example.earthtalk.domain.debate.entity.VoteStatus;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.earthtalk.domain.debate.entity.*;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.earthtalk.domain.debate.dto.CreateDebateRoomRequest;
import com.example.earthtalk.domain.debate.dto.DebateRoomResponse;
import com.example.earthtalk.domain.debate.dto.DebateUserResponse;
import com.example.earthtalk.domain.debate.dto.VoteRequest;
import com.example.earthtalk.domain.debate.dto.WaitRoomResponse;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.store.DebateRoomStore;
import com.example.earthtalk.domain.debate.store.DebateUserStore;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.repository.NewsRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
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
@Slf4j
@RequiredArgsConstructor
public class DebateRoomService {

	private final DebateRoomStore debateRoomStore;
	private final DebateRepository debateRepository;
	private final NewsRepository newsRepository;
	private final UserRepository userRepository;
	private final DebateUserStore debateUserStore;
	private final DebateParticipantsRepository debateParticipantsRepository;
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
		log.info("Service - createDebateRoom : resultEnabled = {}", request.isResultEnabled());
		News news = null;
		if (request.getNewsId() != null) {
			Long newsId = Long.valueOf(request.getNewsId().toString());
			try {
				news = newsRepository.findById(newsId)
					.orElse(null); // news가 없으면 null로 처리
			} catch (Exception e) {
				log.error("News 조회 중 오류 발생: {}", e.getMessage(), e);
			}
		}
		try {
			Debate debate = Debate.builder()
				.uuid(UUID.fromString(roomId))
				.news(news)
				.title(request.getTitle())
				.description(request.getDescription())
				.member(request.getMemberNumber())
				.continent(request.getContinent())
				.category(request.getCategory())
				.speakCount(request.getSpeakCount())
				.resultEnabled(request.isResultEnabled())
				.time(request.getTime())
				.cachedTime(LocalDateTime.now())
				.status(RoomType.WAITING) // 기본 상태 설정
				.agreeNumber(0L) // 초기 찬성 수
				.disagreeNumber(0L) // 초기 반대 수
				.neutralNumber(0L) // 초기 중립 수
				.build();

			debateRepository.save(debate);
			debateRoomStore.put(debate);

		} catch (Exception e) {
			log.error("토론방 생성 중 오류 발생: {}", e.getMessage(), e);

			// 원본 예외의 상세 정보 추출
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String fullStackTrace = sw.toString();

			// 추가적인 디버깅 정보 로깅
			log.error("전체 스택 트레이스: {}", fullStackTrace);

			// 요청 파라미터 로깅 (민감한 정보 주의)
			log.error("요청 파라미터: {}", request.toString());

			// 원본 예외를 그대로 다시 던짐
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

	public Page<Debate> getFinishDebateRooms(String query, ContinentType continent, CategoryType category, MemberNumberType member, int page, String sort) {
		Pageable pageable = PageRequest.of(page, 15);
		return debateRepository.findFinishDebatesByParams(query, continent, category, member, sort, pageable);
	}

	/**
	 * 주어진 roomId에 해당하는 채팅방 정보를 저장소에서 제거합니다.
	 *
	 * @param roomId 채팅방의 고유 식별자
	 */
	public void removeDebateRoom(String roomId) {
		debateRoomStore.remove(roomId);
	}

	@Transactional
	public FlagType processDebateResult(UUID roomId, VoteStatus voteStatus) {
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		Set<User> modifiedUsers = new HashSet<>();
		boolean proWins = true, draw = false;
		if (debate.isResultEnabled()) {
			proWins = voteStatus.getPro() > voteStatus.getCon();
			draw = voteStatus.getPro().equals(voteStatus.getCon());

			for (DebateParticipants participants : debate.getParticipants()) {
				User user = participants.getUser();

				if (draw) {
					user.incrementDrawNumber();
				} else if ((participants.getPosition() == FlagType.PRO && proWins) ||
					(participants.getPosition() != FlagType.PRO && !proWins)) {
					user.incrementWinNumber();
				} else {
					user.incrementDefeatNumber();
				}

				modifiedUsers.add(user);
			}
		}
		userRepository.saveAll(modifiedUsers);

		debate.updateVoteCounts(voteStatus.getPro(), voteStatus.getCon(), voteStatus.getNeutral());
		debateRepository.save(debate);
		return draw ? FlagType.NO_POSITION : (proWins ? FlagType.PRO : FlagType.CON);
	}

	public void updateStatus(String roomId) {
		Debate debate = debateRepository.findByUuid(UUID.fromString(roomId))
				.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		debate.updateRoomType(RoomType.CLOSED);
	}

	public WaitRoomResponse buildWaitRoomResponse(Debate debate, UUID roomId) {
		Set<String> proUsers = debateUserStore.getProUsers(roomId.toString());
		Set<DebateUserResponse> proResponse = convertUsernamesToUserResponses(proUsers);

		Set<String> conUsers  =debateUserStore.getConUsers(roomId.toString());
		Set<DebateUserResponse> conResponse = convertUsernamesToUserResponses(conUsers);

		return WaitRoomResponse.builder()
			.roomId(debate.getId())
			.title(debate.getTitle())
			.description(debate.getDescription())
			.memberNumberType(debate.getMember().getValue())
			.categoryType(debate.getCategory())
			.continentType(debate.getContinent())
			.newsUrl(debate.getNews() != null ? debate.getNews().getLink() : null)
			.status(debate.getStatus())
			.timeType(debate.getTime().getValue())
			.speakCountType(debate.getSpeakCount().getValue())
			.proUsers(proResponse)
			.conUsers(conResponse)
			.build();
	}

	private Set<DebateUserResponse> convertUsernamesToUserResponses(Set<String> usernames) {
		return usernames.stream()
			.map(userRepository::findByNickname)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(user -> DebateUserResponse.builder()
				.id(user.getId())
				.nickname(user.getNickname())
				.email(user.getEmail())
				.introduction(user.getIntroduction())
				.profileUrl(user.getProfileUrl())
				.winNumber(user.getWinNumber())
				.defeatNumber(user.getDefeatNumber())
				.drawNumber(user.getDrawNumber())
				.build())
			.collect(Collectors.toSet());
	}

	public DebateRoomResponse buildDebateRoomResponse(Debate debate, UUID roomId, boolean includeParticipants) {
		DebateRoomResponse.DebateRoomResponseBuilder builder = DebateRoomResponse.builder()
			.roomId(debate.getId())
			.title(debate.getTitle())
			.description(debate.getDescription())
			.memberNumberType(debate.getMember().getValue())
			.categoryType(debate.getCategory())
			.continentType(debate.getContinent())
			.newsUrl(debate.getNews() != null ? debate.getNews().getLink() : null)
			.status(debate.getStatus())
			.timeType(debate.getTime().getValue())
			.speakCountType(debate.getSpeakCount().getValue());

		if (includeParticipants) {
			List<DebateUserResponse> participants = debateParticipantsRepository.findByDebate_Uuid(roomId)
				.stream()
				.map(dp -> new DebateUserResponse(
					dp.getUser().getId(),
					dp.getUser().getEmail(),
					dp.getUser().getNickname(),
					dp.getUser().getIntroduction(),
					dp.getUser().getProfileUrl(),
					dp.getUser().getWinNumber(),
					dp.getUser().getDefeatNumber(),
					dp.getUser().getDrawNumber()
				))
				.toList();
			builder.participants(participants);
		}

		return builder.build();
	}


}
