package com.example.earthtalk.domain.news.dto.response;

import com.example.earthtalk.global.constant.ContinentType;

public record NewsDetailResponse(
    Long like,
    Long mark,
    String title,
    String link,
    ContinentType continent,
    boolean liked,
    boolean marked
) {

}