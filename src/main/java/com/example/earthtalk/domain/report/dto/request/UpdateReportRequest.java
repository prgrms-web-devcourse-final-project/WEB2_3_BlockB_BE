package com.example.earthtalk.domain.report.dto.request;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ResultType;


public record UpdateReportRequest(ResultType result, String reportContent) {

    public Report toEntity() {
        return Report.builder()
                .resultType(result)
                .reportContent(reportContent)
                .build();
    }
}
