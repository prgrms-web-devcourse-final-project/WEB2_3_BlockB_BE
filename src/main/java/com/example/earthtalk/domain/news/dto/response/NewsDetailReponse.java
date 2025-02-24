package com.example.earthtalk.domain.news.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsDetailReponse {
    private Long like;
    private Long mark;
    private String link;
    private boolean liked;
    private boolean marked;
}
