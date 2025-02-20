package com.example.earthtalk.domain.news.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum NewsType {

    @JsonProperty("JOONGANG")
    JOONGANG("중앙일보"),

    @JsonProperty("HANI")
    HANI("한겨레"),

    @JsonProperty("HANKYUNG")
    HANKYUNG("한국경제"),

    @JsonProperty("HANKOOK")
    HANKOOK("한국일보"),

    @JsonProperty("SEGYE")
    SEGYE("세계일보");

    private final String value;

    NewsType(String value) {
        this.value = value;
    }
}
