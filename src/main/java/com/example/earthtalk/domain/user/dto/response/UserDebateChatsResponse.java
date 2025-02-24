package com.example.earthtalk.domain.user.dto.response;
import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.DebateRole;
import com.example.earthtalk.domain.debate.entity.FlagType;
import java.time.LocalDateTime;

public record UserDebateChatsResponse(
    Long userId,
    DebateRole role,
    FlagType position,
    String debateContent,
    LocalDateTime debateChatcreatedAt
) {}


