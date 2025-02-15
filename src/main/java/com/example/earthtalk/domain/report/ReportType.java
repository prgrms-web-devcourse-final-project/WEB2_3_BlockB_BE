package com.example.earthtalk.domain.report;

import lombok.Getter;

@Getter
public enum ReportType {
    CHAT, //실시간 채팅 신고
    DEBATE; //토론방 신고
}
