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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Autowired
    private final ReportRepository reportRepository;
    @Autowired
    private final ObserverChatRepository observerChatRepository;
    @Autowired
    private final DebateChatRepository debateChatRepository;

    // 신고하는 로직 간단하게 구현해놨습니다. 예외처리 따로 안되어있어요.
    // 각 위치에서 신고에 대한 기능 만들 때 예외 처리 해야합니다.
    public Long saveReport(InsertReportRequest request) {
        Report report = request.toEntity();
        return reportRepository.save(report).getId();
    }

    // 신고들을 필터링하여 List 로 가져오는 메서드
    public List<ReportListResponse> getReports(String q, ReportType reportType, ResultType resultType) {
        List<Report> reports = reportRepository.getReportsByParams(q, reportType, resultType);

        List<ReportListResponse> responses = new ArrayList<>();
        for(Report report : reports) {
            if (report == null) {
                return null;
            }
            responses.add(ReportListResponse.from(report));
        }

        return responses;
    }

    // 하나의 신고에 대한 상세 조회하는 메서드
    public ReportDetailResponse getReportById(Long id) {
        Report report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return null;
        }

        String reportContent = getTargetContent(report);

        return ReportDetailResponse.from(report, reportContent);
    }

    // 신고를 처리하는 메서드
    public Long updateReport(Long id, UpdateReportRequest request) {
        Report report = reportRepository.findById(id).orElse(null);
        if(report == null) {
            return null;
        }
        report.updateReport(request);
        return reportRepository.save(report).getId();
    }

    // 이미 처리된 신고를 복구하는 메서드
    public Long restoreReport(Long id) {
        Report report = reportRepository.findById(id).orElse(null);
        if(report == null) {
            return null;
        }
        report.updateReport(null);
        return reportRepository.save(report).getId();
    }


    // 신고된 타입 유형에따라 신고된 내용을 조회하는 메서드
    String getTargetContent(Report report) {
        if(report.getTargetType() == TargetType.CHAT) {
            ObserverChat observerChat = observerChatRepository.findById(report.getId()).orElse(null);
            if (observerChat != null) {
                return observerChat.getContent();
            }
        }

        if(report.getTargetType() == TargetType.DEBATE) {
            DebateChat debateChat = debateChatRepository.findById(report.getId()).orElse(null);
            if (debateChat != null) {
                return debateChat.getContent();
            }
        }

        return null;
    }
}
