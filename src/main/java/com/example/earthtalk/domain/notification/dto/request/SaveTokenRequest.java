package com.example.earthtalk.domain.notification.dto.request;

import com.example.earthtalk.domain.user.entity.User;

public record SaveTokenRequest(Long userId, String token) {

    public User toEntity(User user) {
        return User.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .introduction(user.getIntroduction())
                .profileUrl(user.getProfileUrl())
                .winNumber(user.getWinNumber())
                .drawNumber(user.getDrawNumber())
                .defeatNumber(user.getDefeatNumber())
                .role(user.getRole())
                .socialType(user.getSocialType())
                .socialId(user.getSocialId())
                .FCMToken(token)
                .build();
    }
}
