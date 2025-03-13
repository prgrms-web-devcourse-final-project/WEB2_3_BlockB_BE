package com.example.earthtalk.domain.debate.controller;

import com.example.earthtalk.domain.debate.dto.TurnInfoResponse;
import com.example.earthtalk.domain.debate.service.DebateTurnManagementService;
import com.example.earthtalk.domain.debate.store.VoteStore;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.exception.NotFoundException;
import java.util.UUID;

import com.example.earthtalk.domain.debate.entity.*;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.global.constant.ContinentType;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.earthtalk.domain.debate.dto.DebateRoomResponse;
import com.example.earthtalk.domain.debate.dto.VoteRequest;
import com.example.earthtalk.domain.debate.dto.VoteResponse;
import com.example.earthtalk.domain.debate.dto.WaitRoomResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.debate.service.DebateRoomService;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/debates")
@RequiredArgsConstructor
@Tag(name = "debateRoom", description = "채팅방 관련 API")
public class DebateRoomController {

	private final DebateRepository debateRepository;
	private final DebateRoomService debateRoomService;
	private final DebateTurnManagementService debateTurnManagementService;
	private final VoteStore voteStore;

	@Operation(summary = "토론방 상세 조회 API", description = "토론방의 UUID로 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론방 정보를 성공적으로 조회했습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다.")
	})
	@GetMapping("/{uuid}")
	public ResponseEntity<ApiResponse<DebateRoomResponse>> getDebateRoom(
		@PathVariable("uuid") String uuid
	) {
		log.info("토론방 상세 조회 api 접근");
		UUID roomId = UUID.fromString(uuid);
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		log.info("debate method 조회 {}" , debate);
		DebateRoomResponse response = debateRoomService.buildDebateRoomResponse(debate, roomId);
		log.info("user 정보 조회 pro : {}, con : {}", response.getProUsers(), response.getConUsers());
		log.info("response 조회 완료 {}" , response);
		return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
	}

	@Operation(summary = "종료된 토론방 리스트 조회 API", description = "status 가 closed 에 대한 토론방의 리스트를 조회합니다.")
	@GetMapping("/debateRoom/finished")
	public ResponseEntity<ApiResponse<Page<DebateRoomResponse>>> getFinishedDebateRoom(
			@RequestParam(value = "q", required = false) String q,
			@RequestParam(value = "continent", required = false) ContinentType continent,
			@RequestParam(value = "category", required = false)CategoryType category,
			@RequestParam(value = "member", required = false) MemberNumberType member,
			@RequestParam(value = "p", required = false, defaultValue = "1") int page,
			@RequestParam(value = "sort", required = false, defaultValue = "recent") String sort
	) {
		page = page <= 1 ? 0 : page - 1;
		Page<DebateRoomResponse> debates = debateRoomService.getFinishDebateRooms(q, continent, category, member, page, sort);
		return ResponseEntity.ok(ApiResponse.createSuccess(debates));
	}

	@Operation(summary = "관전자 토론방 상세 조회 API", description = "토론방의 UUID로 관전자용 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론방 정보를 성공적으로 조회했습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다.")
	})
	@GetMapping("/observer/{uuid}")
	public ResponseEntity<ApiResponse<DebateRoomResponse>> getObserverDebateRoom(
		@PathVariable("uuid") String uuid
	) {
		UUID roomId = UUID.fromString(uuid);
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));
		DebateRoomResponse response = debateRoomService.buildDebateRoomResponse(debate, roomId);
		return ResponseEntity.ok().body(ApiResponse.createSuccess(response));

	}

	@Operation(summary = "관전 대기실 토론방 조회 API", description = "토론방의 UUID로 관전 대기실 정보를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토론방 정보를 성공적으로 조회했습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다.")
	})
	@GetMapping("/waitroom/{uuid}")
	public ResponseEntity<ApiResponse<WaitRoomResponse>> getObserverWaitRoom(
		@PathVariable("uuid") String uuid
	) {
		UUID roomId = UUID.fromString(uuid);
		Debate debate = debateRepository.findByUuid(roomId)
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));

		WaitRoomResponse response = debateRoomService.buildWaitRoomResponse(debate, roomId);

		return ResponseEntity.ok().body(ApiResponse.createSuccess(response));
	}



	@Operation(summary = "투표 업데이트 API", description = "토론방의 투표 수(찬성, 반대, 중립)를 업데이트하고 업데이트 된 결과에 따라 유저의 승/패를 추가적으로 업데이트합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투표 수가 성공적으로 업데이트되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다."),
	})
	@PutMapping("/vote/{uuid}")
	public ResponseEntity<ApiResponse<Void>> putVote(
		@PathVariable("uuid") String uuid,
		@RequestBody VoteRequest request) {
		voteStore.processVote(UUID.fromString(uuid), request);
		return ResponseEntity.ok(ApiResponse.createSuccess(null));
	}

	@Operation(summary = "투표 조회 API", description = "토론방의 현재 투표 수(찬성, 반대, 중립)를 조회합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투표 수가 성공적으로 조회되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다."),
	})
	@GetMapping("/vote/{uuid}")
	public ResponseEntity<ApiResponse<VoteResponse>> getVote(
		@PathVariable("uuid") String uuid
	) {
		Debate debate = debateRepository.findByUuid(UUID.fromString(uuid))
			.orElseThrow(() -> new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage()));

		VoteResponse response = VoteResponse.builder()
			.agreeNumber(debate.getAgreeNumber())
			.disagreeNumber(debate.getDisagreeNumber())
			.neutralNumber(debate.getNeutralNumber())
			.build();

		return ResponseEntity.ok(ApiResponse.createSuccess(response));
	}

	@Operation(summary = "토론 턴 조회 API", description = "토론방 중간에 입장한 관전자가 현재 턴 카운트와 남은시간을 조회할 수 있습니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 토론방을 찾을 수 없습니다."),
	})
	@GetMapping("/turn/{uuid}")
	public ResponseEntity<ApiResponse<TurnInfoResponse>> getCurrentTurn(@PathVariable String uuid){
		TimeType timeType = debateRepository.findByUuid(UUID.fromString(uuid))
			.orElseThrow(()->new NotFoundException(ErrorCode.DEBATEROOM_NOT_FOUND)).getTime();
		TurnInfoResponse response = debateTurnManagementService.getCurrentTurn(UUID.fromString(uuid), timeType);
		return ResponseEntity.ok(ApiResponse.createSuccess(response));
	}
}
