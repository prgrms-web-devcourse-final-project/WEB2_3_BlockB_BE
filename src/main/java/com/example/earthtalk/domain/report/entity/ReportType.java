package com.example.earthtalk.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportType {
    OBSCENITY("선정성"),
    SPAM("스팸"),
    CUSS("욕설"),
    FLOODING("도배"),
    LEAKAGE("유출"),
    DODGE("탈주");

    private final String value;

    ReportType(String value) {
        this.value = value;
    }
}
