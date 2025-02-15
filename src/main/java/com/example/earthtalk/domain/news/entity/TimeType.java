package com.example.earthtalk.domain.news.entity;

import lombok.Getter;

/**
 * 토론 시간 종류
 */
@Getter
public enum TimeType {
    T5(5),
    T10(10),
    T15(15);

    private final Integer value;

    TimeType(Integer value) {
        this.value = value;
    }
}
