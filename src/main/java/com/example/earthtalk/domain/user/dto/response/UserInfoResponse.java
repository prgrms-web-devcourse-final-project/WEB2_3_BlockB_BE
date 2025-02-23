package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.user.entity.User;

public record UserInfoResponse(
    Long userId,
    String nickname,
    String profile,
    String introduction,
    Long totalFollowers,
    Long totalFollowees,
    Long wins,
    Long draws,
    Long losses
) {public static UserInfoResponse from(User user, Long totalFollowers, Long totalFollowees) {
        return new UserInfoResponse(
            user.getId(),
            user.getNickname(),
            user.getProfileUrl(),
            user.getIntroduction(),
            totalFollowers,
            totalFollowees,
            user.getWinNumber(),
            user.getDrawNumber(),
            user.getDefeatNumber()
        );
    }
}
