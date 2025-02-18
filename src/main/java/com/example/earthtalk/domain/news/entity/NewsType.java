package com.example.earthtalk.domain.news.entity;

import lombok.Getter;

@Getter
public enum NewsType {
    SEOUL("서울신문"),
    KHAN("경향신문"),
    HANKYUNG("한국경제"),
    DONGA("동아일보"),
    HANKOOK("한국일보"),
    SEGYE("세계일보");

    private final String value;

    NewsType(String value) {
        this.value = value;
    }
}
