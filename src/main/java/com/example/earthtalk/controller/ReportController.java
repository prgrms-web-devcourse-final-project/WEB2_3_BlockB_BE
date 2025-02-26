package com.example.earthtalk.controller;

import com.example.earthtalk.domain.report.dto.request.InsertReportRequest;
import com.example.earthtalk.domain.report.service.ReportService;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "신고 생성 API", description = "토론방 또는 관련 엔티티에 대한 신고를 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "신고가 성공적으로 생성되었습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자 또는 토론방을 찾을 수 없습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "내부 서버 오류가 발생했습니다.")
    })
    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> postReport(@RequestBody InsertReportRequest request) {
        Long reportId = reportService.saveReport(request);
        if (reportId == null) {
            throw new IllegalArgumentException(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.createSuccess(null));
    }
}
