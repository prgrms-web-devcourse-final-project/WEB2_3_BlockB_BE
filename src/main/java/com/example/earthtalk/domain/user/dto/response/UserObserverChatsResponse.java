package com.example.earthtalk.domain.user.dto.response;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.FlagType;
import java.time.LocalDateTime;

public record UserObserverChatsResponse(
    CategoryType category,
    String title,
    Long observerUserId,
    String observerNickname,
    FlagType position,
    String observerContent,
    LocalDateTime observerChatcreatedAt
) {}


