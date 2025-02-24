package com.example.earthtalk.domain.report.dto.request;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.TargetType;
import com.example.earthtalk.domain.user.entity.User;

public record InsertReportRequest(
        User user,
        User targetUser,
        Long targetRoomId,
        TargetType targetType,
        String content,
        ReportType reportType
) {

    public Report toEntity() {
        return Report.builder()
                .user(user)
                .targetUser(targetUser)
                .targetRoomId(targetRoomId)
                .targetType(targetType)
                .content(content)
                .reportType(reportType)
                .build();
    }
}
