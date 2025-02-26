package com.example.earthtalk.domain.debate.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.earthtalk.domain.debate.dto.DebateRoomResponse;
import com.example.earthtalk.domain.debate.dto.DebateUserResponse;
import com.example.earthtalk.domain.debate.dto.VoteRequest;
import com.example.earthtalk.domain.debate.dto.VoteResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.DebateParticipants;
import com.example.earthtalk.domain.debate.entity.FlagType;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.domain.report.dto.request.InsertReportRequest;
import com.example.earthtalk.domain.report.service.ReportService;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/debates")
@RequiredArgsConstructor
@Tag(name = "debateRoom", description = "채팅방 관련 API")
public class DebateRoomController {

	private final DebateRepository debateRepository;
	private final DebateParticipantsRepository debateParticipantsRepository;
	private final UserRepository userRepository;
	private final ReportService reportService;
	private final DebateRoomService debateRoomService;

	@Operation(summary = "토론방 상세 조회 API", description = "토론방의 UUID로 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론방 정보를 성공적으로 조회했습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다.")
	})
	@GetMapping("/{uuid}")
	public ResponseEntity<ApiResponse<Object>> getDebateRoom(
		@PathVariable("uuid") String uuid
	) {
		UUID roomId = UUID.fromString(uuid);
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		DebateRoomResponse response = buildDebateRoomResponse(debate, roomId, true);

		return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
	}

	@Operation(summary = "관전자 토론방 상세 조회 API", description = "토론방의 UUID로 관전자용 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론방 정보를 성공적으로 조회했습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다.")
	})
	@GetMapping("/observer/{uuid}")
	public ResponseEntity<ApiResponse<Object>> getObserverDebateRoom(
		@PathVariable("uuid") String uuid
	) {
		UUID roomId = UUID.fromString(uuid);
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		DebateRoomResponse response = buildDebateRoomResponse(debate, roomId, true);
		return ResponseEntity.ok().body(ApiResponse.createSuccess(response));

	}

	@Operation(summary = "관전 대기실 토론방 조회 API", description = "토론방의 UUID로 관전 대기실 정보를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론방 정보를 성공적으로 조회했습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다.")
	})
	@GetMapping("/observer/waitroom/{uuid}")
	public ResponseEntity<ApiResponse<Object>> getObserverWaitRoom(
		@PathVariable("uuid") String uuid
	) {
		UUID roomId = UUID.fromString(uuid);
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		DebateRoomResponse response = buildDebateRoomResponse(debate, roomId, false);
		return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
	}

	@Operation(summary = "투표 업데이트 API", description = "토론방의 투표 수(찬성, 반대, 중립)를 업데이트하고 업데이트 된 결과에 따라 유저의 승/패를 추가적으로 업데이트합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투표 수가 성공적으로 업데이트되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다."),
	})
	@PutMapping("/vote/{roomId}")
	public ResponseEntity<ApiResponse<Object>> putVote(
		@PathVariable("roomId") Long roomId,
		@RequestBody VoteRequest request) {
		Debate debate = debateRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));

		if (request.getNeutralNumber() < 0 || request.getDisagreeNumber() < 0 || request.getAgreeNumber() < 0) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}

		if (debate.getParticipants() == null || debate.getParticipants().isEmpty()) {
			throw new IllegalArgumentException(ErrorCode.DEBATE_NO_PARTICIPANTS.getMessage());
		}

		debateRoomService.processDebateResult(debate, request);

		debateRepository.save(debate);

		return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());

	}

	@Operation(summary = "투표 조회 API", description = "토론방의 현재 투표 수(찬성, 반대, 중립)를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투표 수가 성공적으로 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다."),
	})
	@GetMapping("/vote/{roomId}")
	public ResponseEntity<ApiResponse<Object>> getVote(
		@PathVariable("roomId") Long roomId
	) {
		Debate debate = debateRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));

		VoteResponse response = VoteResponse.builder()
			.agreeNumber(debate.getAgreeNumber())
			.disagreeNumber(debate.getDisagreeNumber())
			.neutralNumber(debate.getNeutralNumber())
			.build();

		return ResponseEntity.ok(ApiResponse.createSuccess(response));
	}

	private DebateRoomResponse buildDebateRoomResponse(Debate debate, UUID roomId, boolean includeParticipants) {
		DebateRoomResponse.DebateRoomResponseBuilder builder = DebateRoomResponse.builder()
			.roomId(debate.getId())
			.title(debate.getTitle())
			.description(debate.getDescription())
			.memberNumberType(debate.getMember().getValue())
			.categoryType(debate.getCategory())
			.continentType(debate.getContinent())
			.newsUrl(debate.getNews().getLink())
			.status(debate.getStatus())
			.timeType(debate.getTime().getValue())
			.speakCountType(debate.getSpeakCount().getValue());

		if (includeParticipants) {
			List<DebateUserResponse> participants = debateParticipantsRepository.findByDebate_Uuid(roomId)
				.stream()
				.map(dp -> new DebateUserResponse(
					dp.getUser().getId(),
					dp.getUser().getNickname(),
					dp.getPosition(),
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
