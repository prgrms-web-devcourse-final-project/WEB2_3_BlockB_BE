package com.example.earthtalk.domain.news.dto.response;

public record NewsDetailResponse(
    Long like,
    Long mark,
    String link,
    boolean liked,
    boolean marked
) {
    public NewsDetailResponse(Long like, Long mark, String link, boolean liked, boolean marked) {
        this.like = like;
        this.mark = mark;
        this.link = link;
        this.liked = liked;
        this.marked = marked;
    }
}