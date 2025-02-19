package com.example.earthtalk.domain.report.entity;

public enum ResultType {
    WARNING("경고"),
    SUSPENSION("일시정지"),
    BAN("영구정지"),
    NONE("처리없음"),
    UNKNOWN("미처리");

    private final String value;

    ResultType(String value) {
        this.value = value;
    }
}
