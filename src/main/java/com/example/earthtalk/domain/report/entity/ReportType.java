package com.example.earthtalk.domain.report.entity;

import lombok.Getter;

@Getter
public enum ReportType {
    OBSCENITY("음란성/선정성"),
    SPAM("스팸 및 광고"),
    CUSS("욕설/인신공격"),
    FLOODING("도배"),
    LEAKAGE("개인정보 유출"),
    DODGE("사유없는 탈주");

    private final String value;

    ReportType(String value) {
        this.value = value;
    }
}
