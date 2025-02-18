package com.example.earthtalk.domain.report;

import lombok.Getter;

@Getter
public enum ReportType {
    CUSS, //실시간 채팅 신고
    SPAM,
    ILLEGAL; //토론방 신고
}
