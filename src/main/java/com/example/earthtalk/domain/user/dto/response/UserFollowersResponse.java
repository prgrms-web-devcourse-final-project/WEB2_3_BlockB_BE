package com.example.earthtalk.domain.user.dto.response;

public record UserFollowersResponse(
    Long followerId,
    String nickname,
    String profile,
    String introduction
) {}


