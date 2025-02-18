package com.example.earthtalk.domain.news.entity;

import com.example.earthtalk.global.baseTime.BaseTimeEntity;
import com.example.earthtalk.global.constant.ContinentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "news")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class News extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String link;

    @Column(nullable = false)
    private String imgUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsType newsType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContinentType continent;

    @Column(nullable = false)
    private String deliveryTime; // 기사 작성일자

    private String country;
}
