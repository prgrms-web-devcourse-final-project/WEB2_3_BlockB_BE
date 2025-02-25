package com.example.earthtalk.domain.report.dto.response;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;

import java.time.LocalDateTime;

public record ReportListResponse(
        Long id,
        String nickname,
        String targetNickname,
        String reportType,
        String reportResult,
        String status,
        LocalDateTime createdAt)
{
    public static ReportListResponse from(Report report) {
        return new ReportListResponse(report.getId(),
                report.getUser().getNickname(),
                report.getTargetUser().getNickname(),
                report.getReportType().getValue(),
                Report.getStringByResultType(report.getResultType()),
                ReportListResponse.getStatus(report.getResultType()),
                report.getCreatedAt());
    }

    public static String getStatus(ResultType resultType) {
        if(resultType == ResultType.UNKNOWN) {
            return resultType.getValue();
        } else {
            return "처리 완료";
        }
    }
}
