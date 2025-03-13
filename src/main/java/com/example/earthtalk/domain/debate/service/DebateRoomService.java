package com.example.earthtalk.domain.debate.service;

import com.example.earthtalk.domain.debate.dto.VoteResponse;
import com.example.earthtalk.domain.debate.entity.VoteStatus;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.earthtalk.domain.debate.entity.*;
import com.example.earthtalk.domain.debate.store.ObserverRoomStore;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;
import com.example.earthtalk.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final ObserverRoomStore observerRoomStore;

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
		// 메서드 시작 로그: 요청 정보와 함께 시작됨
		log.info("createDebateRoom 시작 - 요청 정보: {}", request);

		// roomId 생성 및 로그 기록
		String roomId = UUID.randomUUID().toString();
		log.debug("생성된 roomId: {}", roomId);

		// 요청 결과 활성화 여부 로깅
		log.info("Service - createDebateRoom : resultEnabled = {}", request.isResultEnabled());

		News news = null;
		if (request.getNewsId() != null) {
			log.debug("NewsId 존재 - 요청된 NewsId: {}", request.getNewsId());
			Long newsId = Long.valueOf(request.getNewsId().toString());
			try {
				news = newsRepository.findById(newsId).orElse(null);
				if (news != null) {
					log.debug("News 조회 성공 - newsId: {}", newsId);
				} else {
					log.warn("News 조회 결과 null - newsId: {}", newsId);
				}
			} catch (Exception e) {
				log.error("News 조회 중 오류 발생 - newsId: {} | 메시지: {}", newsId, e.getMessage(), e);
			}
		} else {
			log.debug("요청에 NewsId 미포함");
		}

		try {
			// Debate 객체 생성 전 필드 값 로깅
			log.debug("Debate 객체 생성 시작 - title: {}, description: {}, member: {}, continent: {}, category: {}, speakCount: {}, time: {}",
				request.getTitle(), request.getDescription(), request.getMemberNumber(),
				request.getContinent(), request.getCategory(), request.getSpeakCount(), request.getTime());

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
				.agreeNumber(0L)         // 초기 찬성 수
				.disagreeNumber(0L)      // 초기 반대 수
				.neutralNumber(0L)       // 초기 중립 수
				.build();
			log.debug("Debate 객체 생성 완료 - {}", debate);

			debateRepository.save(debate);
			log.info("Debate 저장 완료 - Debate ID: {}", debate.getId());

			debateRoomStore.put(debate);
			log.info("DebateRoomStore에 Debate 추가 완료 - Debate ID: {}", debate.getId());

			observerRoomStore.initializeRoom(roomId);
			log.info("ObserverRoomStore 초기화 완료 - roomId: {}", roomId);

			// 토론방 생성 완료 로그
			log.info("createDebateRoom 완료 - 생성된 토론방 ID: {}", roomId);
		} catch (Exception e) {
			log.error("토론방 생성 중 오류 발생: {}", e.getMessage(), e);

			// 예외 발생 시 전체 스택 트레이스 로깅
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String fullStackTrace = sw.toString();
			log.error("전체 스택 트레이스: {}", fullStackTrace);

			// 요청 파라미터도 로깅 (민감한 정보 주의)
			log.error("요청 파라미터: {}", request.toString());

			// 예외 발생 시 에러 코드에 해당하는 메시지 반환
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

	public Page<DebateRoomResponse> getFinishDebateRooms(String query, ContinentType continent, CategoryType category, MemberNumberType member, int page, String sort) {
		Pageable pageable = PageRequest.of(page, 15);
		Page<Debate> debatePage = debateRepository.findFinishDebatesByParams(query, continent, category, member, sort, pageable);
		List<DebateRoomResponse> responses = new ArrayList<>();
		for(Debate debate : debatePage.getContent()) {
			if (debate == null) {
				throw new NotFoundException(ErrorCode.DEBATEROOM_NOT_FOUND);
			}
			responses.add(buildDebateRoomResponse(debate, debate.getUuid()));
		}
		return new PageImpl<>(responses, pageable, debatePage.getTotalElements());
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
		log.info("Service - processDebateResult : resultEnabled = {} | roomId = {}", debate.isResultEnabled(), roomId);
		boolean proWins = true, draw = false;
		if (debate.isResultEnabled()) {
			proWins = voteStatus.getPro() > voteStatus.getCon();
			draw = voteStatus.getPro().equals(voteStatus.getCon());

			log.info("Service - processDebateResult : pro = {}", voteStatus.getPro());
			log.info("Service - processDebateResult : con = {}", voteStatus.getCon());
			log.info("Service - processDebateResult : participants = {}", debate.getParticipants().size());
			try {
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
			}catch (Exception e){
				e.printStackTrace();
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

		Set<String> conUsers  = debateUserStore.getConUsers(roomId.toString());
		Set<DebateUserResponse> conResponse = convertUsernamesToUserResponses(conUsers);

		return WaitRoomResponse.builder()
			.roomId(debate.getUuid())
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
			.resultEnabled(debate.isResultEnabled())
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

	public DebateRoomResponse buildDebateRoomResponse(Debate debate, UUID roomId) {

		Set<String> proUsers = debateUserStore.getProUsers(roomId.toString());
		Set<DebateUserResponse> proResponse = new HashSet<>();
		for (String proUser : proUsers) {
			User user = userRepository.findByNickname(proUser)
					.orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
			DebateUserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.introduction(user.getIntroduction())
				.profileUrl(user.getProfileUrl())
				.winNumber(user.getWinNumber())
				.drawNumber(user.getDrawNumber())
				.defeatNumber(user.getDefeatNumber())
				.position(FlagType.PRO)
				.build();
		}

		Set<DebateUserResponse> conResponse = new HashSet<>();

		// 로그: Con 사용자 목록 조회 시작
		Set<String> conUsers = debateUserStore.getConUsers(roomId.toString());
		for (String conUser : conUsers) {
			User user = userRepository.findByNickname(conUser)
				.orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));
			DebateUserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.nickname(user.getNickname())
				.introduction(user.getIntroduction())
				.profileUrl(user.getProfileUrl())
				.winNumber(user.getWinNumber())
				.drawNumber(user.getDrawNumber())
				.defeatNumber(user.getDefeatNumber())
				.position(FlagType.CON)
				.build();
		}

		// 로그: DebateRoomResponse 빌더를 사용하여 응답 객체 생성 시작
		DebateRoomResponse response = DebateRoomResponse.builder()
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
			.proUsers(proResponse)
			.conUsers(conResponse)
			.resultEnabled(debate.isResultEnabled())
			.build();

		return response;
	}

}
