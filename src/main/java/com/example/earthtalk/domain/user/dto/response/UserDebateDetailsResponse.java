package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.debate.entity.CategoryType;
import com.example.earthtalk.domain.debate.entity.Debate;
import com.example.earthtalk.domain.debate.entity.SpeakCountType;
import com.example.earthtalk.domain.news.entity.MemberNumberType;
import com.example.earthtalk.domain.news.entity.TimeType;
import com.example.earthtalk.global.constant.ContinentType;

public record UserDebateDetailsResponse(
    Long debateId,
    String title,
    String description,
    String link,
    ContinentType continent,
    CategoryType category,
    MemberNumberType member,
    TimeType time,
    SpeakCountType speakCount,
    boolean resultEnabled,
    Long agreeNumber,
    Long disagreeNumber,
    Long neutralNumber
) { public static UserDebateDetailsResponse from(Debate debate,String link) {
    return new UserDebateDetailsResponse(
        debate.getId(),
        debate.getTitle(),
        debate.getDescription(),
        link,
        debate.getContinent(),
        debate.getCategory(),
        debate.getMember(),
        debate.getTime(),
        debate.getSpeakCount(),
        debate.isResultEnabled(),
        debate.getAgreeNumber(),
        debate.getDisagreeNumber(),
        debate.getNeutralNumber()
    );
}
}
