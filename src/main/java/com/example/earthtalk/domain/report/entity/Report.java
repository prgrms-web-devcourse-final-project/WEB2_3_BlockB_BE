package com.example.earthtalk.domain.report.entity;

import com.example.earthtalk.domain.report.dto.request.UpdateReportRequest;
import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.global.baseTime.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 신고한 회원

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser; // 신고된 회원

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType; // 신고 대상 유형

    private Long targetRoomId; // 신고 대상 유형 id

    @Column(nullable = false)
    private String content; // 신고 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType; // 신고 사유

    private String reportContent; // 신고 처리한 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultType resultType; // 신고 처리 유형

    private LocalDateTime reportedAt; // 신고 처리 날짜

    public void updateReport(UpdateReportRequest request) {
        if(request == null) {
            this.reportContent = null;
            this.resultType = ResultType.UNKNOWN;
            this.reportedAt = null;

        } else {
            Report updateReport = request.toEntity();
            this.reportContent = updateReport.getReportContent();
            this.resultType = updateReport.getResultType();
            this.reportedAt = LocalDateTime.now();
        }
    }

    public static String getStringByResultType(ResultType resultType) {
        if(resultType == null) {
            return null;
        }
        return resultType.getValue();
    }
}
