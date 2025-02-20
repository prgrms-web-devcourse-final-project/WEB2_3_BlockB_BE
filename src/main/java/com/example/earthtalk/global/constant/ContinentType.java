package com.example.earthtalk.global.constant;

import lombok.Getter;

@Getter
public enum ContinentType {
    AS("아시아/호주"),
    AM("미국/중남미"),
    EU("유럽"),
    JP("일본"),
    CN("중국"),
    AF("아프리카/중동"),
    KR("한국");

    private final String value;

    ContinentType(String value) {
        this.value = value;
    }
}
