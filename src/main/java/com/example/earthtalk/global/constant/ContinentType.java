package com.example.earthtalk.global.constant;

import lombok.Getter;

@Getter
public enum ContinentType {
    AS("아시아"),
    NA("북미"),
    SA("남미"),
    EU("유럽"),
    OC("오세아니아"),
    AF("아프리카"),
    ETC("기타");

    private final String value;

    ContinentType(String value) {
        this.value = value;
    }
}
