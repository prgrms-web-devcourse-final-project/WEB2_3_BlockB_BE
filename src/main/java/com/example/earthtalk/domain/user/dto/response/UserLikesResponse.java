package com.example.earthtalk.domain.user.dto.response;
import com.example.earthtalk.domain.news.entity.News;
import com.example.earthtalk.global.constant.ContinentType;
import java.time.LocalDateTime;

public record UserLikesResponse(
    Long newsId,
    boolean isLike,
    String title,
    ContinentType continent,
    LocalDateTime createdAt
) {}


