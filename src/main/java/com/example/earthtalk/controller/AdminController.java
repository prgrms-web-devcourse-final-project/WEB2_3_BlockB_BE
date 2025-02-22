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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ReportService reportService;

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<ReportListResponse>>> getReports(@RequestParam(value = "q", required = false) String q,
                                                          @RequestParam(value = "type", required = false) ReportType reportType,
                                                          @RequestParam(value = "result", required = false) ResultType resultType,
                                                          @RequestParam(value = "p", required = false, defaultValue = "1") int page) {
        page = page <= 1 ? 0 : page - 1;
        Page<ReportListResponse> reports = reportService.getReports(q, reportType, resultType, page);
        return ResponseEntity.ok(ApiResponse.createSuccess(reports));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReportById(@PathVariable("reportId") Long reportId) {
        ReportDetailResponse report = reportService.getReportById(reportId);
        return ResponseEntity.ok(ApiResponse.createSuccess(report));
    }

    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<Long>> putReportById(@PathVariable("reportId") Long reportId,
                                                              @RequestBody UpdateReportRequest request) throws Exception{
        Long id = reportService.updateReport(reportId, request);
        return ResponseEntity.ok(ApiResponse.createSuccess(id));
    }

    @PutMapping("/reports/{reportId}/restore")
    public ResponseEntity<ApiResponse<Long>> putReportRestoreById(@PathVariable("reportId") Long reportId) {
        Long id = reportService.restoreReport(reportId);
        return ResponseEntity.ok(ApiResponse.createSuccess(id));
    }
}
