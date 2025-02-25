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
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String introduction; // 한줄 소개

    @Column(nullable = false)
    private String profileUrl; // 프로필 이미지

    private Long winNumber = 0L; // 승리 횟수

    private Long drawNumber = 0L; // 무승부 횟수

    private Long defeatNumber = 0L; // 패배 횟수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    private String socialId; // 로그인한 소셜 타입 식별값

    @Builder
    public User(String email, String nickname, String introduction, String profileUrl,
        Long winNumber, Long drawNumber, Long defeatNumber, Role role, SocialType socialType,
        String socialId) {
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
    }

    public void incrementWinNumber() {
        this.winNumber++;
    }

    public void incrementDrawNumber() {
        this.drawNumber++;
    }

    public void incrementDefeatNumber() {
        this.defeatNumber++;
    }
}
