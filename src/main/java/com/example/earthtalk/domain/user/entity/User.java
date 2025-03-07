package com.example.earthtalk.domain.user.entity;

import com.example.earthtalk.domain.report.entity.ResultType;
import com.example.earthtalk.global.baseTime.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
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

    private Long winNumber = 0L; // 승리 횟수

    private Long drawNumber = 0L; // 무승부 횟수

    private Long defeatNumber = 0L; // 패배 횟수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = false)
    private String socialId; // 소셜로그인 식별값

    private String socialAccessToken;

    private String socialRefreshToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatusType accountStatusType;

    private LocalDateTime suspendedAt;

    @Builder
    public User(String email, String nickname, String profileUrl, Role role, SocialType socialType,
        String socialId, String socialAccessToken, String socialRefreshToken) {
        this.email = email;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
        this.socialAccessToken = socialAccessToken;
        this.socialRefreshToken = socialRefreshToken;
        this.accountStatusType = AccountStatusType.ACTIVE;
    }

    public void updateRole(Role newRole) {
        this.role = newRole;
    }

    public void updateSignupInfo(String nickname, String introduction) {
        this.nickname = nickname;
        this.introduction = introduction;
    }

    public void updateTokens(String socialAccessToken, String socialRefreshToken) {
        this.socialAccessToken = socialAccessToken;
        this.socialRefreshToken = socialRefreshToken;
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

    public void reportUser(ResultType resultType) {
        if (resultType == ResultType.BAN) {
            this.accountStatusType = AccountStatusType.BANNED;
            this.role = Role.ROLE_BANNED;
            return;
        }

        if (resultType == ResultType.SUSPENSION ||
                (resultType == ResultType.WARNING && this.accountStatusType == AccountStatusType.WARNING)) {
            this.accountStatusType = AccountStatusType.SUSPENDED;
            this.suspendedAt = LocalDateTime.now();
            this.role = Role.ROLE_BANNED;
            return;
        }

        this.accountStatusType = AccountStatusType.WARNING;
    }

    public void restoreUser() {
        this.accountStatusType = AccountStatusType.ACTIVE;
        this.role = Role.ROLE_MEMBER;
    }

    public boolean isSuspended() {
        return this.accountStatusType == AccountStatusType.SUSPENDED;
    }

    public boolean isBanned() {
        return this.accountStatusType == AccountStatusType.BANNED;
    }

    public boolean isSuspensionPeriodOver() {
        return this.suspendedAt != null && LocalDateTime.now().isAfter(this.suspendedAt.plusDays(3));
    }
}
