package com.example.earthtalk.domain.report.dto.request;

import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.TargetType;
import com.example.earthtalk.domain.user.entity.User;

public record InsertReportRequest(
        Long userId,
        Long targetUserId,
        TargetType targetType,
        Long targetRoomId,
        String content,
        ReportType reportType
) {

    public Report toEntity(User user, User targetUser) {
        return Report.builder()
                .user(user)
                .targetUser(targetUser)
                .targetType(targetType)
                .targetRoomId(targetRoomId)
                .content(content)
                .reportType(reportType)
                .build();
    }
}
