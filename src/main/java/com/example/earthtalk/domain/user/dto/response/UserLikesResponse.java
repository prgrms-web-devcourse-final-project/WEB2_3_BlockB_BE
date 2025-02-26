package com.example.earthtalk.domain.user.dto.response;
import com.example.earthtalk.global.constant.ContinentType;
import java.time.LocalDateTime;

public record UserLikesResponse(
    Long newsId,
    String title,
    ContinentType continent,
    LocalDateTime createdAt
) {}


