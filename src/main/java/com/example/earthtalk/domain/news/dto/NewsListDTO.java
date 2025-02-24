package com.example.earthtalk.domain.news.dto;

import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.domain.news.entity.NewsType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsListDTO {
    private Long id;
    private String title;
    private String content;
    private String imgUrl;
    private String newsType;
    private LocalDateTime deliveryTime;
    private Long like;
    private Long bookmark;

    public NewsListDTO(News news, Long like, Long bookmark) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.content = news.getContent();
        this.imgUrl = news.getImgUrl();
        this.newsType = news.getNewsType().getValue();
        this.deliveryTime = news.getDeliveryTime();
        this.like = like;
        this.bookmark = bookmark;
    }
    public void setNewsType(NewsType newsType) {
        this.newsType = newsType.getValue();
    }
}