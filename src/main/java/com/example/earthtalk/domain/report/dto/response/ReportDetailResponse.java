package com.example.earthtalk.domain.report.dto.response;

import com.example.earthtalk.domain.chat.ObserverChat;
import com.example.earthtalk.domain.chat.repository.ObserverChatRepository;
import com.example.earthtalk.domain.debate.entity.DebateChat;
import com.example.earthtalk.domain.debate.repository.DebateChatRepository;
import com.example.earthtalk.domain.report.entity.Report;
import com.example.earthtalk.domain.report.entity.ReportType;
import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.domain.report.entity.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public record ReportDetailResponse(
        Long id,
        String nickname,
        TargetType targetType,
        Long targetRoomId,
        Long targetId,
        String targetNickname,
        String targetContent,
        String content,
        ReportType reportType,
        ResultType reportResult,
        String reportContent,
        LocalDateTime createdAt
) {

    public static ReportDetailResponse from(Report report, String targetContent) {
        return new ReportDetailResponse(
                report.getId(),
                report.getUser().getNickname(),
                report.getTargetType(),
                report.getTargetRoomId(),
                report.getTargetUser().getId(),
                report.getTargetUser().getNickname(),
                targetContent,
                report.getContent(),
                report.getReportType(),
                report.getResultType(),
                report.getReportContent(),
                report.getCreatedAt()
        );
    }
}
