package com.example.earthtalk.domain.user.dto.response;

import com.example.earthtalk.domain.user.entity.User;

public record UserDebateRoomInfoResponse(
    Long userId,
    String nickname,
    String profile,
    Long wins,
    Long draws,
    Long losses
) {
    public static UserDebateRoomInfoResponse from(User user, Long totalFollowers, Long totalFollowees) {
        return new UserDebateRoomInfoResponse(
            user.getId(),
            user.getNickname(),
            user.getProfileUrl(),
            user.getWinNumber(),
            user.getDrawNumber(),
            user.getDefeatNumber()
        );
    }
}
