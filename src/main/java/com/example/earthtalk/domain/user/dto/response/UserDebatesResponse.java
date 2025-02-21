package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.RoomType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.domain.user.entity.User;

public record UserDebatesResponse(
    Long debateId,
    CategoryType category,
    String title,
    TimeType time,
    MemberNumberType member,
    Long userId,
    RoomType status
) { }
