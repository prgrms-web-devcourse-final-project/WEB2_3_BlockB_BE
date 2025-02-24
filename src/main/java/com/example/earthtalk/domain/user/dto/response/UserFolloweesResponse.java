package com.example.earthtalk.domain.user.dto.response;
import com.example.earthtalk.global.constant.ContinentType;
import java.time.LocalDateTime;

public record UserFolloweesResponse(
    Long followeeId,
    String nickname,
    String profile,
    String introduction
) {}


