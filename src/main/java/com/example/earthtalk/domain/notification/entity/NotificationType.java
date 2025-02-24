package com.example.earthtalk.domain.notification.entity;


public enum NotificationType {
    REPORT("신고"),
    CHAT("채팅방"),
    DEBATE("토론방"),
    FOLLOW("팔로우");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }
}
