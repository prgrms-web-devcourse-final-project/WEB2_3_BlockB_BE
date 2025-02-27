package com.example.earthtalk.domain.report.dto.request;

import com.example.earthtalk.domain.report.entity.ResultType;


public record UpdateReportRequest(
        Long assignedUserId,
        ResultType result,
        String reportContent) {
}
