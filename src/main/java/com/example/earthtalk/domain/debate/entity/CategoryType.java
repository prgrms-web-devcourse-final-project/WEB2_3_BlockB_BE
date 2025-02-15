package com.example.earthtalk.domain.debate.entity;

import lombok.Getter;

@Getter
public enum CategoryType {
    PO("정치"),
    EC("경제"),
    SO("사회"),
    CU("문화"),
    EN("연예"),
    SP("스포츠"),
    IT("IT"),
    CO("칼럼"),
    ETC("기타");

    private final String value;

    CategoryType(String value) {
        this.value = value;
    }
}
