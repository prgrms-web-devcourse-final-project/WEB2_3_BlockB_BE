package com.example.earthtalk.domain.news.entity;

import lombok.Getter;

/**
 * 토론방 형태 종류
 */
@Getter
public enum MemberNumberType {
    T1("1:1"),
    T2("3:3");

    private final String value;

    MemberNumberType(String value) {
        this.value = value;
    }
}
