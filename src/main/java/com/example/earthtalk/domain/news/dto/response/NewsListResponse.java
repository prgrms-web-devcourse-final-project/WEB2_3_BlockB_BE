package com.example.earthtalk.domain.news.dto.response;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.NewsType;
import com.example.earthtalk.global.constant.ContinentType;
import java.time.LocalDateTime;

public record NewsListResponse(
    Long id,
    String title,
    String content,
    String imgUrl,
    NewsType newsType,
    String newsName,
    ContinentType continentType,
    LocalDateTime deliveryTime,
    Long like,
    Long bookmark
) {
    public NewsListResponse(News news, Long like, Long bookmark) {
        this(
            news.getId(),
            news.getTitle(),
            news.getContent(),
            news.getImgUrl(),
            news.getNewsType(),
            news.getNewsType().getValue(),
            news.getContinent(),
            news.getDeliveryTime(),
            like,
            bookmark
        );
    }
}