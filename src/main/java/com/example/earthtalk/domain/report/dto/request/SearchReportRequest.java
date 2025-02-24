package com.example.earthtalk.domain.report.dto.request;

import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;

public record SearchReportRequest(String q, ReportType type, ResultType result) {
}
