package com.example.earthtalk.domain.notification.entity;

import lombok.Getter;

@Getter
public enum StatusType {
    UNREAD("안읽음"),
    READ("읽음");

    private final String value;

    StatusType(String value) {
        this.value = value;
    }
}
