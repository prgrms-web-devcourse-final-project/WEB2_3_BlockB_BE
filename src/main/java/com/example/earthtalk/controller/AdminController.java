package com.example.earthtalk.controller;

import com.example.earthtalk.domain.report.dto.request.UpdateReportRequest;
import com.example.earthtalk.domain.report.dto.response.ReportDetailResponse;
import com.example.earthtalk.domain.report.dto.response.ReportListResponse;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.domain.report.service.ReportService;
import com.example.earthtalk.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private final ReportService reportService;

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Object>> getReports(@RequestParam(value = "q", required = false) String q,
                                                                            @RequestParam(value = "type", required = false) ReportType reportType,
                                                                            @RequestParam(value = "result", required = false) ResultType resultType) {
        List<ReportListResponse> reports = reportService.getReports(q, reportType, resultType);
        if(reports == null) {
            return ResponseEntity.ok(ApiResponse.createError("신고리스트가 존재하지 않습니다."));
        }
        return ResponseEntity.ok(ApiResponse.createSuccess(reports));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<Object>> getReportsById(@PathVariable("reportId") Long reportId) {
        ReportDetailResponse report = reportService.getReportById(reportId);
        if(report == null) {
            return ResponseEntity.ok(ApiResponse.createError("조회하신 신고가 존재하지 않습니다."));
        }
        return ResponseEntity.ok(ApiResponse.createSuccess(report));
    }

    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<Object>> putReportsById(@PathVariable("reportId") Long reportId,
                                                              @RequestBody UpdateReportRequest request) {
        Long id = reportService.updateReport(reportId, request);
        if (id == null) {
            return ResponseEntity.ok(ApiResponse.createError("조회하신 신고가 존재하지 않습니다."));
        }
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }

    @PutMapping("/reports/{reportId}/restore")
    public ResponseEntity<ApiResponse<Object>> putReportRestoreById(@PathVariable("reportId") Long reportId) {
        Long id = reportService.restoreReport(reportId);
        if (id == null) {
            return ResponseEntity.ok(ApiResponse.createError("조회하신 신고가 존재하지 않습니다."));
        }
        return ResponseEntity.ok(ApiResponse.createSuccessWithNoData());
    }
}
