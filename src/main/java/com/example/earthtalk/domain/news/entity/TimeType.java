package com.example.earthtalk.domain.news.entity;

import lombok.Getter;

/**
 * 토론 시간 종류
 */
@Getter
public enum TimeType {
    T3(30),
    T4(40),
    T5(50),
    T6(60),
    T9(90),
    T12(120),
    T15(150);

    private final Integer value;

    TimeType(Integer value) {
        this.value = value;
    }
}
