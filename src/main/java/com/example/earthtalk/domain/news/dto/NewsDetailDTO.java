package com.example.earthtalk.domain.news.dto;

import com.example.earthtalk.domain.news.entity.Like;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsDetailDTO {
    private Long like;
    private Long mark;
    private String link;
    private boolean liked;
    private boolean marked;
}
