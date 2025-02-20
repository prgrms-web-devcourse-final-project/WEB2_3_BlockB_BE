package com.example.earthtalk.domain.report.entity;

import lombok.Getter;

@Getter
public enum TargetType {
    CHAT("채팅방"), // 실시간 채팅 신고
    DEBATE("토론방"), // 토론방 신고
    PROFILE("프로필"); // 유저 직접 신고

    private final String value;

    TargetType(String value) {
        this.value = value;
    }
}
