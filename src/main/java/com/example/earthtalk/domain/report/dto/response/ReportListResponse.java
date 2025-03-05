package com.example.earthtalk.domain.report.dto.response;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ResultType;
import java.time.format.DateTimeFormatter;

public record ReportListResponse(
        Long id,
        String nickname,
        String targetNickname,
        String assignedNickname,
        String reportType,
        String reportResult,
        String status,
        String createdAt,
        String reportedAt)
{
    public static ReportListResponse from(Report report, DateTimeFormatter formatter) {
        return new ReportListResponse(report.getId(),
                report.getUser().getNickname(),
                report.getTargetUser().getNickname(),
                report.getAssignedUserNickname(),
                report.getReportType().getValue(),
                Report.getStringByResultType(report.getResultType()),
                ReportListResponse.getStatus(report.getResultType()),
                report.getCreatedAt().format(formatter),
                getReportedAt(report, formatter));
    }

    private static String getStatus(ResultType resultType) {
        if(resultType == ResultType.UNKNOWN) {
            return resultType.getValue();
        } else {
            return "처리 완료";
        }
    }


    private static String getReportedAt(Report report, DateTimeFormatter formatter) {
        if (report.getResultType() == ResultType.UNKNOWN) {
            return null;
        } else {
            return report.getUpdatedAt().format(formatter);
        }
    }
}
