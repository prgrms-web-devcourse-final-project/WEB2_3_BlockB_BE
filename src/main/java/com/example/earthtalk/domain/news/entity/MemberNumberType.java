package com.example.earthtalk.domain.news.entity;

import lombok.Getter;

/**
 * 토론방 형태 종류
 */
@Getter
public enum MemberNumberType {
    T1(1),
    T2(3);

    private final int value;

    MemberNumberType(int value) {
        this.value = value;
    }
}
