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
    public NewsDetailResponse(Long like, Long mark, String title, String link, ContinentType continent, boolean liked, boolean marked) {
        this.like = like;
        this.mark = mark;
        this.title = title;
        this.link = link;
        this.continent = continent;
        this.liked = liked;
        this.marked = marked;
    }
}