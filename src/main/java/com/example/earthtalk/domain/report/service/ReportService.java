package com.example.earthtalk.domain.report.service;

import com.example.earthtalk.domain.debate.repository.DebateRepository;
import com.example.earthtalk.domain.notification.dto.request.SendNotificationRequest;
import com.example.earthtalk.domain.notification.entity.NotificationType;
import com.example.earthtalk.domain.notification.service.NotificationService;
import com.example.earthtalk.domain.report.dto.request.InsertReportRequest;
import com.example.earthtalk.domain.report.dto.request.UpdateReportRequest;
import com.example.earthtalk.domain.report.dto.response.ReportDetailResponse;
import com.example.earthtalk.domain.report.dto.response.ReportListResponse;
import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.domain.report.entity.TargetType;
import com.example.earthtalk.domain.report.repository.ReportRepository;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.domain.user.repository.UserRepository;
import com.example.earthtalk.global.exception.ErrorCode;
import com.example.earthtalk.global.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final NotificationService notificationService;
    private final DebateRepository debateRepository;
    private final UserRepository userRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 신고하는 로직 간단하게 구현해놨습니다. 예외처리 따로 안되어있어요.
    // 각 위치에서 신고에 대한 기능 만들 때 예외 처리 해야합니다.
    public Long saveReport(InsertReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(ErrorCode.INVALID_REQUEST_BODY.getMessage());
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        User targetUser = userRepository.findById(request.targetUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (request.targetType() == TargetType.CHAT && !debateRepository.existsById(request.targetRoomId())) {
            throw new IllegalArgumentException(ErrorCode.DEBATEROOM_NOT_FOUND.getMessage());
        }


        Report report = request.toEntity(user, targetUser);
        return reportRepository.save(report).getId();
    }

    // 신고들을 필터링하여 List 로 가져오는 메서드
    public Page<ReportListResponse> getReports(String q, ReportType reportType, ResultType resultType, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        Page<Report> reports = reportRepository.findReportsByParams(q, reportType, resultType, pageable);

        List<ReportListResponse> responses = new ArrayList<>();
        for(Report report : reports.getContent()) {
            if (report == null) {
                throw new NotFoundException(ErrorCode.REPORT_NOT_FOUND);
            }
            responses.add(ReportListResponse.from(report, formatter));
        }

        return new PageImpl<>(responses, pageable, reports.getTotalElements());
    }

    // 하나의 신고에 대한 상세 조회하는 메서드
    public ReportDetailResponse getReportById(Long id) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
        return ReportDetailResponse.from(report, formatter);
    }

    // 신고를 처리하는 메서드 - 알림 추가
    @Transactional
    public Long updateReport(Long reportId, UpdateReportRequest request) {

        // 에러 확인용 request 값 확인 로그 추가 - 추후에 삭제할 것
        log.info("reportId : ${} , request : ${}", reportId, request.toString());

        // 받은 id 값으로 report 조회
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
        log.info("report 조회");
        // 신고 처리 담당자 조회
        User assignedUser = userRepository.findById(request.assignedUserId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        log.info("assignedUser 조회");
        // 신고에 관한 내용 처리
        report.updateReport(request, assignedUser);

        // 유저에 관한 정보 업데이트
        User user = userRepository.findById(report.getTargetUser().getId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.reportUser(report.getResultType());
        log.info("신고당한 유저 조회");
        // 알림 전송
        notificationService.sendNotification(new SendNotificationRequest(
                report.getTargetUser().getId(),
                NotificationType.REPORT,
                report.getId(),
                null
        ));

        // 신고에 관한 내용 업데이트
        return report.getId();
    }

    // 이미 처리된 신고를 복구하는 메서드
    @Transactional
    public Long restoreReport(Long id) {
        // 신고에 관한 내용 조회 및 복구
        Report report = reportRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.REPORT_NOT_FOUND));
        report.resetReport();

        // 유저에 관한 내용 조회 및 업데이트
        User user = userRepository.findById(report.getTargetUser().getId()).orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
        user.restoreUser();

        // 신고에 관한 내용 업데이트
        return report.getId();
    }
}
