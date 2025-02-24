package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;

public record UserDebatesResponse(
    Long debateId,
    CategoryType category,
    String title,
    TimeType time,
    MemberNumberType member,
    RoomType status,
    Boolean isParticipant
) { }
