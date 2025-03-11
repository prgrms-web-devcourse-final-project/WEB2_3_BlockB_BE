package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import java.util.UUID;

public record UserDebatesResponse(
    UUID debateId,
    CategoryType category,
    String title,
    TimeType time,
    MemberNumberType member,
    RoomType status,
    Boolean isParticipant
) { }
