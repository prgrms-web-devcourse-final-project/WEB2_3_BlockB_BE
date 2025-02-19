package com.example.earthtalk.domain.user.userDto;

public record UserInfoDto (
    Long userId,
    String nickname,
    String profile,
    String introduction,
    Long totalFollowers,
    Long totalFollowees,
    Long wins,
    Long draws,
    Long losses
) {}
