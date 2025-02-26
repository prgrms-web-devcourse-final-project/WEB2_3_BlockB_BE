package com.example.earthtalk.domain.user.dto.response;

public record UserFolloweesResponse(
    Long followeeId,
    String nickname,
    String profile,
    String introduction
) {}


