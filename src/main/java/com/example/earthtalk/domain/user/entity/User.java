package com.example.earthtalk.domain.user.entity;

import com.example.earthtalk.global.baseTime.BaseTimeEntity;
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

@Entity(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // 랜덤으로 형성한 식별값

    @Column(nullable = false, unique = true)
    private String nickname;

    private String introduction; // 한줄 소개

    private String profileUrl; // 프로필 이미지

    private Long winNumber; // 승리 횟수

    private Long drawNumber; // 무승부 횟수

    private Long defeatNumber; // 패배 횟수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    private String socialId; // 소셜로그인 식별값

    private String FCMToken; // 알림을 위한 FCM 토큰

    @Builder
    public User(String email, String nickname, String introduction, String profileUrl,
        Long winNumber, Long drawNumber, Long defeatNumber, Role role, SocialType socialType,
        String socialId, String FCMToken) {
        this.email = email;
        this.nickname = nickname;
        this.introduction = introduction;
        this.profileUrl = profileUrl;
        this.winNumber = winNumber;
        this.drawNumber = drawNumber;
        this.defeatNumber = defeatNumber;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
        this.FCMToken = FCMToken;
    }
}
