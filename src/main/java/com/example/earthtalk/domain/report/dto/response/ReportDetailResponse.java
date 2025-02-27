package com.example.earthtalk.domain.report.dto.response;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.domain.report.entity.TargetType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ReportDetailResponse(
        Long id,
        String nickname,
        TargetType targetType,
        Long targetRoomId,
        Long targetUserId,
        String targetNickname,
        String content,
        String reportType,
        String reportResult,
        String reportContent,
        String createdAt,
        String reportedAt
) {

    public static ReportDetailResponse from(Report report, DateTimeFormatter formatter) {
        return new ReportDetailResponse(
                report.getId(),
                report.getUser().getNickname(),
                report.getTargetType(),
                report.getTargetRoomId(),
                report.getTargetUser().getId(),
                report.getTargetUser().getNickname(),
                report.getContent(),
                report.getReportType().getValue(),
                Report.getStringByResultType(report.getResultType()),
                report.getReportContent(),
                report.getCreatedAt().format(formatter),
                getReportedAt(report, formatter)
        );
    }

    private static String getReportedAt(Report report, DateTimeFormatter formatter) {
        if (report.getResultType() == ResultType.UNKNOWN) {
            return null;
        } else {
            return report.getUpdatedAt().format(formatter);
        }
    }
}
