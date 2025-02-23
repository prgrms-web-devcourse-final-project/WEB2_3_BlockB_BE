package com.example.earthtalk.controller;

import com.example.earthtalk.domain.report.dto.request.UpdateReportRequest;
import com.example.earthtalk.domain.report.dto.response.ReportDetailResponse;
import com.example.earthtalk.domain.report.dto.response.ReportListResponse;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.domain.report.service.ReportService;
import com.example.earthtalk.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ReportService reportService;

    @Operation(summary = "신고 조회 API 입니다.", description = "각 파라미터에 해당하는 값을 통해 조회된 신고들을 페이지네이션하여 반환합니다.")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<ReportListResponse>>> getReports(@RequestParam(value = "q", required = false) String q,
                                                          @RequestParam(value = "type", required = false) ReportType reportType,
                                                          @RequestParam(value = "result", required = false) ResultType resultType,
                                                          @RequestParam(value = "p", required = false, defaultValue = "1") int page) {
        page = page <= 1 ? 0 : page - 1;
        Page<ReportListResponse> reports = reportService.getReports(q, reportType, resultType, page);
        return ResponseEntity.ok(ApiResponse.createSuccess(reports));
    }

    @Operation(summary = "신고 상세 조회 API 입니다.", description = "reportId 에 해당하는 신고의 상세한 정보를 조회합니다.")
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReportById(@PathVariable("reportId") Long reportId) {
        ReportDetailResponse report = reportService.getReportById(reportId);
        return ResponseEntity.ok(ApiResponse.createSuccess(report));
    }

    @Operation(summary = "신고 처리 API 입니다.", description = "reportId 에 해당하는 신고의 처리를 담당합니다.")
    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<Long>> putReportById(@PathVariable("reportId") Long reportId,
                                                              @RequestBody UpdateReportRequest request) throws Exception{
        Long id = reportService.updateReport(reportId, request);
        return ResponseEntity.ok(ApiResponse.createSuccess(id));
    }

    @Operation(summary = "신고 복구 API 입니다.", description = "이미 처리된 신고를 미처리 상태로 복구합니다.")
    @PutMapping("/reports/{reportId}/restore")
    public ResponseEntity<ApiResponse<Long>> putReportRestoreById(@PathVariable("reportId") Long reportId) {
        Long id = reportService.restoreReport(reportId);
        return ResponseEntity.ok(ApiResponse.createSuccess(id));
    }
}
