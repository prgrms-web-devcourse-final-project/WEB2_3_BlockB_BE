package com.example.earthtalk.domain.report.service;

import com.example.earthtalk.domain.chat.ObserverChat;
import com.example.earthtalk.domain.chat.repository.ObserverChatRepository;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.report.dto.request.InsertReportRequest;
import com.example.earthtalk.domain.report.dto.request.UpdateReportRequest;
import com.example.earthtalk.domain.report.dto.response.ReportDetailResponse;
import com.example.earthtalk.domain.report.dto.response.ReportListResponse;
import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.domain.report.entity.TargetType;
import com.example.earthtalk.domain.report.repository.ReportRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ObserverChatRepository observerChatRepository;
    private final DebateChatRepository debateChatRepository;

    // 신고하는 로직 간단하게 구현해놨습니다. 예외처리 따로 안되어있어요.
    // 각 위치에서 신고에 대한 기능 만들 때 예외 처리 해야합니다.
    public Long saveReport(InsertReportRequest request) {
        Report report = request.toEntity();
        return reportRepository.save(report).getId();
    }

    // 신고들을 필터링하여 List 로 가져오는 메서드
    public Page<ReportListResponse> getReports(String q, ReportType reportType, ResultType resultType, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        Page<Report> reports = reportRepository.findReportsByParams(q, reportType, resultType, pageable);

        List<ReportListResponse> responses = new ArrayList<>();
        for(Report report : reports.getContent()) {
            if (report == null) {
                return null;
            }
            responses.add(ReportListResponse.from(report));
        }

        return new PageImpl<>(responses, pageable, reports.getTotalElements());
    }

    // 하나의 신고에 대한 상세 조회하는 메서드
    public ReportDetailResponse getReportById(Long id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
        return ReportDetailResponse.from(report);
    }

    // 신고를 처리하는 메서드
    public Long updateReport(Long id, UpdateReportRequest request) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
        report.updateReport(request);
        return reportRepository.save(report).getId();
    }

    // 이미 처리된 신고를 복구하는 메서드
    public Long restoreReport(Long id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
        report.updateReport(null);
        return reportRepository.save(report).getId();
    }
}
