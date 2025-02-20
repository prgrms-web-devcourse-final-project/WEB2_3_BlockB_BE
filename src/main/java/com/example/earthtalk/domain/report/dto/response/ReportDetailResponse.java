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
        Long targetUserId,
        String targetNickname,
        String content,
        String reportType,
        String reportResult,
        String reportContent,
        LocalDateTime createdAt
) {

    public static ReportDetailResponse from(Report report) {
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
                report.getCreatedAt()
        );
    }
}
