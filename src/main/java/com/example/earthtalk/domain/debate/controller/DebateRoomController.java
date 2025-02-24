package com.example.earthtalk.domain.debate.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.earthtalk.domain.debate.dto.DebateRoomResponse;
import com.example.earthtalk.domain.debate.dto.DebateUserResponse;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.repository.DebateParticipantsRepository;
import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.report.dto.request.InsertReportRequest;
import com.example.earthtalk.domain.report.service.ReportService;
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

	@Operation(summary = "신고 생성 API", description = "토론방 또는 관련 엔티티에 대한 신고를 생성합니다.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고가 성공적으로 생성되었습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자 또는 토론방을 찾을 수 없습니다."),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "내부 서버 오류가 발생했습니다.")
	})
	@PostMapping("/report")
	public ResponseEntity<ApiResponse<Object>> saveReport(@RequestBody InsertReportRequest request) {
		if (request == null) {
			throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
		}

		if (!userRepository.existsById(request.targetId())) {
			throw new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage());
		}

		if (!debateRepository.existsById(request.targetRoomId())) {
			throw new IllegalArgumentException(ErrorCode.CHAT_NOT_FOUND.getMessage());
		}

		Long reportId = reportService.saveReport(request);
		if (reportId == null) {
			throw new IllegalArgumentException(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
		}

		return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
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
