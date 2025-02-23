package com.example.earthtalk.domain.debate.entity;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.baseTime.BaseTimeEntity;
import com.example.earthtalk.global.constant.ContinentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "debates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Debate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id")
    private News news;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private MemberNumberType member;

    @Enumerated(EnumType.STRING)
    private ContinentType continent;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    private TimeType time;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private RoomType status;

    @Column(nullable = false)
    private Long agreeNumber;

    @Column(nullable = false)
    private Long disagreeNumber;

    @PrePersist
    public void setDefaultPosition() {
        if (this.status == null) {
            this.status = RoomType.DEBATE;
        }
        if (this.agreeNumber == null) {
            this.agreeNumber = 0L;
        }
        if (this.disagreeNumber == null) {
            this.disagreeNumber = 0L;
        }
    }
}
